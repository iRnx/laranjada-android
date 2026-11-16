# Troca de episódio — referência Web, correção Mobile e handoff TV

## Escopo e fontes verificadas

- Web: `templates/static/assets/js/player/player-autonext.js`, `templates/static/assets/js/modules/player_presence.js`, `templates/static/assets/js/modules/watching_heartbeat.js` no repositório `iRnx/laranjada`.
- Mobile: `feature/player/PlayerScreen.kt` e `PlayerViewModel.kt` no repositório `iRnx/laranjada-android`.
- TV: `feature/player/TvPlayerScreen.kt` e `core/navigation/AppNavigation.kt` no repositório `iRnx/laranjada-tv`.

## Comportamento comprovado na Web

1. O card de próximo episódio aparece na janela de 30 s finais.
2. O clique em **Assistir agora** e o evento **ended** chamam `navigateToNextEpisode()`.
3. A função usa `window.location.assign(nextUrl)`, após atraso curto, navegando para uma nova página — não reutiliza o mesmo elemento HLS.
4. Há listeners separados de `ended`, `pagehide` e `beforeunload` para gravar progresso, e de `ended`/`pagehide` para enviar Stop. São requisições independentes e não há garantia de que todas completem antes da navegação.
5. O novo documento inicia seu próprio fluxo de reserva/autorização.

**Não copiar literalmente** o redirecionamento de página, nem presumir que o JavaScript espera o Stop terminar. O app Android deve manter sua sequência explícita: progresso -> Stop -> nova Reserve.

## Mobile — abordagem para teste

- Nas duas transições (manual e término natural), interromper o carregamento anterior antes de substituir o HLS.
- Preservar o salvamento de progresso (manual: posição; natural: `ended`) e encerrar a reserva anterior antes de iniciar a nova.
- Usar `key(reservation.contentType, reservation.contentUuid)` ao compor `HlsPlayer`. O UUID diferente desmonta o ExoPlayer anterior e cria um novo; reautorizações do **mesmo episódio** não devem recriar o player, preservando o tratamento de retorno do background.
- Evitar que erro tardio da sessão anterior interrompa a sessão seguinte; registrar `errorCodeName` e stack trace em `LaranjadaPlayback` sem expor URLs/tokens.
- Não mascarar erro do player novo: um `Source error` que persistir após a nova sessão ser criada precisa de diagnóstico HTTP/CDN/HLS real.
- A branch do PR #5 ainda **não foi compilada nem validada no aparelho**.

## TV — diagnóstico independente, ainda sem implementação

Em `TvPlayerScreen.kt`, a TV guarda estados como:
```kotlin
val stopRequested = remember { AtomicBoolean(false) }
var closing by remember { mutableStateOf(false) }
var reservation by remember { mutableStateOf<TvPlaybackReservation?>(null) }
```

Em `AppNavigation.kt`, `onEpisodeSelected` substitui o último destino Player no `backStack` (alterando `uuid`), mas o composable `TvPlayerScreen` não está envolvido em uma chave explícita por conteúdo.

**Risco:** estados de finalização e a instância anterior de Media3 podem sobreviver à troca. A função que reserva o episódio seguinte testa `stopRequested.get()` e, se verdadeiro, para a reserva nova.

### Proposta para TV (posterior)

- Em `AppNavigation.kt`, usar um `key(current.kind, current.uuid) { TvPlayerScreen(...) }` envolvendo **a chamada inteira existente** e mantendo seus parâmetros/callbacks.
- Garantir que o Stop/progresso do episódio antigo ocorra antes de substituir a rota (já é objetivo de `closePlayer`).
- Confirmar que o cleanup ao desmontar o player não encerra a reserva nova; verificar o ciclo de vida e callbacks do `DisposableEffect`.
- Não copiar os guards de erro do Mobile sem analisar a TV: ela tem `MediaSession`, navegação por DPAD e ciclo de vida próprios.
- Testar início após botão manual, início após `STATE_ENDED`, retorno, pausa, logout/revogação, episódio sem vídeo e fechamento rápido.
- Em caso de **crash**, coletar Logcat com `FATAL EXCEPTION`, `Caused by` e `LaranjadaTvPlayer`; não afirmar que é o mesmo `Source error` sem evidência.

## Matriz mínima de validação

1. Primeiro -> segundo episódio: clicar **Assistir agora**, verificar vídeo, áudio, legenda e progresso.
2. Primeiro -> segundo episódio: deixar terminar, verificar novo HLS e status `ended` do primeiro.
3. Alternar episódios pelo menu; retornar ao episódio anterior.
4. Colocar em background e voltar; renovar autorização **sem reiniciar o mesmo episódio**.
5. Trocar de episódio com Wi-Fi desligado ou CDN retornando 403/404; apresentar erro real e permitir tentativa posterior.
6. Conferir Logcat (Mobile: `LaranjadaPlayback`; TV: `LaranjadaTvPlayer`), com códigos HTTP sem registrar credenciais.

## Observação de distribuição

`prodRelease` do Mobile usa `https://laranjada.zip` como origem de API inicial. A URL efetiva de reprodução é a retornada pelo endpoint de reserva. Verificar `versionCode` antes de distribuir uma nova versão assinada como atualização.
