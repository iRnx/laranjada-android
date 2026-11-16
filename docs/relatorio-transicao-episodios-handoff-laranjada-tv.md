# Relatório técnico — Próximo episódio (Mobile → Android TV)
Data: 15–16/11/2026

## Objetivo
Registrar evidências do erro intermitente na passagem ao próximo episódio, correções implementadas no Mobile, limites das conclusões e abordagem recomendada para análise do Android TV. **Não considerar o problema definitivamente resolvido sem novos testes.**

## Referências efetivamente analisadas
- Mobile `iRnx/laranjada-android`:
  - `feature/player/PlayerScreen.kt`
  - `feature/player/PlayerViewModel.kt`
  - `feature/player/WatchingProgressViewModel.kt`
  - `core/playback/PlaybackAuthorizationStore.kt`
  - `core/playback/PlaybackHttpDataSourceFactory.kt`
- Web/Django `iRnx/laranjada`:
  - `templates/static/assets/js/player/player-autonext.js`
  - `templates/static/assets/js/modules/player_presence.js`
  - `templates/static/assets/js/modules/watching_heartbeat.js`
  - `watching/services/playback_authorization_service.py`
  - `watching/services/playback_reservation_service.py`
- Infra `iRnx/laranjada-infra`:
  - `terraform/modules/cloudflare/workers/stream_gateway/scripts/stream-gateway.js`
- TV `iRnx/laranjada-tv`:
  - `feature/player/TvPlayerScreen.kt`
  - `core/navigation/AppNavigation.kt`

## Sintomas
- Em alguns episódios, o botão **Assistir agora** avança; em outros, não.
- No fim natural (`STATE_ENDED`), às vezes o próximo episódio não abre.
- Erro Media3 `Source error / ERROR_CODE_IO_BAD_HTTP_STATUS`, com HTTP 403.
- Na TV, foi relatado também encerramento/crash do aplicativo; a causa desse crash ainda não foi demonstrada em stack trace.

## Evidência direta — logs Mobile HML em 15/11
### 21:41:33–21:42:19: fim natural problemático
- `21:41:33` — fim natural detectado para episódio `cf884bf1-6564-4030-8dc9-57c63719e714`.
- `21:41:33` — progresso final (`ended`) salvo com sucesso.
- `21:41:34` — Stop `ended` retornou sucesso.
- **Não há registro de nova reserva imediatamente após esse Stop.**
- `21:42:16` — requisições para `video_720p_init.mp4`, `video_480p_init.mp4` e `audio_por_init.mp4` do **episódio antigo** foram negadas com HTTP 403 e **`authorizationAttached=false`**.
- `21:42:19` — Source error do episódio antigo.

### 21:43:04–21:43:06: fim natural bem-sucedido
- Progresso ended → Stop ended → nova Reserve (~0,5 s depois) para episódio `39077e87-6873-4e09-8f7c-536e2e4399d6`.
- Esse contraste demonstra falha **intermitente na decisão/transição**, não falha sistemática do endpoint Reserve.

## Leitura técnica
No Mobile, `stopPlayback()` interrompe Presence/Renew e executa `authorizationStore.clear()`. Se o ExoPlayer da sessão finalizada continuar tentando obter segmentos ou arquivos de inicialização, estes serão requisitados **sem Bearer** e receberão 403 do Stream Gateway. Isso explica os registros de 403 **após Stop**, mas, sozinho, não prova por que o próximo episódio não foi selecionado.

A Web utiliza `window.location.assign(nextUrl)` tanto no botão quanto no evento `ended`, criando um novo player em outra página. O backend salva progresso e faz Stop por eventos independentes. O Mobile deve manter o sequenciamento explícito progresso → Stop → nova Reserve, mas **isolar o player por episódio**.

## Mudanças realizadas no Mobile
- PR #5 (mesclado): `key(reservation.contentType, reservation.contentUuid)` recria o `HlsPlayer`/Media3 em cada troca de episódio; protege erros tardios da sessão anterior.
- PR #6 (mesclado, evidenciado pelos logs do usuário): acrescenta `HLS_HTTP_DENIED status=... host=... path=... authorizationAttached=...` sem registrar bearer/query.
- PR #7 (mesclado): chama `exoPlayer.stop()` após capturar posição/duração no fim natural, **antes** de concluir e invalidar a autorização; procura o primeiro episódio posterior com vídeo (em vez de apenas o item imediatamente posterior); consulta próximo episódio atualizado no callback e registra `AutoNext após Stop` com `next`, `dismissed`, `willAdvance`.
- **Status:** não existem logs posteriores ao PR #7 neste relatório; resultados de novos testes ainda não confirmados.

## Diagnóstico e proposta para Laranjada TV
1. **Reproduzir e capturar Logcat** (`LaranjadaTvPlayer`, `FATAL EXCEPTION`, `Caused by`, Media3 HTTP code). Não atribuir o crash ao HTTP 403 sem stack trace.
2. Revisar `TvPlayerScreen.kt`: `stopRequested = remember { AtomicBoolean(false) }`, `closing = remember { ... }`, `reservation = remember { ... }`. A navegação em `AppNavigation.kt` substitui o UUID do destino Player, mas pode preservar a composição e, com isso, manter `stopRequested=true` de um episódio encerrado.
3. **Correção candidata na TV (ainda não implementada):** em `AppNavigation.kt`, envolver a chamada atual inteira de `TvPlayerScreen(...)` em `key(current.kind, current.uuid) { ... }`. Preservar todos os parâmetros e callbacks. Isso garante novo estado de Compose, ExoPlayer e MediaSession quando o UUID mudar.
4. Verificar especificamente que `closePlayer()` salva progresso e envia Stop antes de substituir a rota; evitar Stop duplo do cleanup `DisposableEffect` sobre sessão nova.
5. No fim natural, interromper leituras do Media3 antigo antes de invalidar a autorização de HLS; não substituir `STATE_ENDED` por Stop sem salvar `ended`.
6. Seleção do próximo episódio: considerar o primeiro posterior com `hasVideo`, não simplesmente o seguinte na lista. A TV já possui essa busca em `nextEpisode`.
7. Quando não houver próximo episódio, manter player encerrado sem continuar requisitando HLS protegido; registrar claramente `next=none`, o índice atual e o motivo de cancelamento.
8. Não mascarar erro real do novo episódio: se ele possuir Reserve própria e retornar 403 **com** Authorization, verificar `playback authorization denied: reason` no Worker Cloudflare ou negativa da origem B2. O 403 com `authorizationAttached=false` **após Stop** da sessão velha tem causa distinta.
9. Testar botão Assistir agora, fim natural, episódio seguinte sem HLS, último episódio da série, navegação para trás, foco remoto, background, atualização de playback, progresso concluído e liberação da vaga/tela do plano.

## Como confirmar a correção
Um caso de sucesso de ponta a ponta deve mostrar:
```
Fim natural detectado
Progress Ended processado
Playback encerrado status=ended
AutoNext após Stop ... willAdvance=true
Playback reservado ... content=episode:<UUID seguinte>
Presence iniciado ...
```
Não deve haver `HLS_HTTP_DENIED` para recursos do episódio encerrado após o Stop. Se `willAdvance=false`, examinar `next=none`, seleção/cancelamento e dados da lista de episódios.

## Evitar
- Não desabilitar Bearer/Cloudflare para contornar 403.
- Não reutilizar autorização do episódio anterior no novo.
- Não supor que todo 403 é falha do B2.
- Não copiar integralmente a lógica Mobile para TV sem considerar MediaSession e navegação por controle remoto.
- Não considerar a correção validada apenas porque o merge concluiu.
