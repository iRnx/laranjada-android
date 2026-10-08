
@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.rnx.laranjada.feature.catalog.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rnx.laranjada.core.design.theme.LaranjadaOrange

data class CatalogFilterOption(
    val label: String,
    val value: String
)

private val CatalogPanel =
    Color(0xFF101417)

private val CatalogMuted =
    Color(0xFFBFC4C8)

@Composable
fun CatalogFilterToolbar(
    rangeLabel: String,
    canPrevious: Boolean,
    canNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSort: () -> Unit,
    onFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.Center,
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            text = rangeLabel,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.size(10.dp)
        )

        IconButton(
            onClick = onPrevious,
            enabled = canPrevious,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.ChevronLeft,
                contentDescription =
                    "Página anterior",
                tint =
                    if (canPrevious) {
                        Color.White
                    } else {
                        Color.DarkGray
                    }
            )
        }

        IconButton(
            onClick = onNext,
            enabled = canNext,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.ChevronRight,
                contentDescription =
                    "Próxima página",
                tint =
                    if (canNext) {
                        Color.White
                    } else {
                        Color.DarkGray
                    }
            )
        }

        Text(
            text = "A-Z",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            modifier = Modifier
                .clickable(
                    onClick = onSort
                )
                .padding(10.dp)
        )

        IconButton(
            onClick = onFilters,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.FilterAlt,
                contentDescription =
                    "Abrir filtros",
                tint = Color.White,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

@Composable
fun CatalogOrderSheet(
    options: List<CatalogFilterOption>,
    selectedValue: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CatalogPanel
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            CatalogSheetTitle(
                title = "Ordenar por",
                onClose = onDismiss
            )

            options.forEach { option ->
                CatalogChoiceRow(
                    label = option.label,
                    selected =
                        option.value == selectedValue,
                    onClick = {
                        onSelect(option.value)
                    }
                )
            }

            Spacer(
                modifier = Modifier.height(30.dp)
            )
        }
    }
}

@Composable
fun CatalogFiltersSheet(
    search: String,
    year: String,
    ratingMin: String,
    type: String,
    years: List<Int>,
    ratings: List<CatalogFilterOption>,
    types: List<CatalogFilterOption>,
    typeTitle: String = "Tipo de conteúdo",
    onApply: (
        search: String,
        year: String,
        rating: String,
        type: String
    ) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var draftSearch by remember(search) {
        mutableStateOf(search)
    }

    var draftYear by remember(year) {
        mutableStateOf(year)
    }

    var draftRating by remember(ratingMin) {
        mutableStateOf(ratingMin)
    }

    var draftType by remember(type) {
        mutableStateOf(type)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CatalogPanel
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 650.dp)
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(horizontal = 20.dp)
        ) {
            CatalogSheetTitle(
                title = "Filtros",
                onClose = onDismiss
            )

            Text(
                text = "Buscar",
                color = CatalogMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            OutlinedTextField(
                value = draftSearch,
                onValueChange = {
                    draftSearch = it
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Pesquisar título",
                        color = CatalogMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector =
                            Icons.Rounded.Search,
                        contentDescription = null
                    )
                },
                keyboardOptions =
                    KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                keyboardActions =
                    KeyboardActions(
                        onSearch = {
                            onApply(
                                draftSearch,
                                draftYear,
                                draftRating,
                                draftType
                            )
                        }
                    ),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedTextColor =
                            Color.White,
                        unfocusedTextColor =
                            Color.White,
                        focusedBorderColor =
                            LaranjadaOrange,
                        unfocusedBorderColor =
                            CatalogMuted,
                        focusedPlaceholderColor =
                            CatalogMuted,
                        unfocusedPlaceholderColor =
                            CatalogMuted
                    ),
                shape = RoundedCornerShape(10.dp)
            )

            CatalogSheetSectionTitle(
                title = "Ano"
            )

            CatalogChoiceRow(
                label = "Todos",
                selected = draftYear.isBlank(),
                onClick = {
                    draftYear = ""
                }
            )

            years.distinct()
                .sortedDescending()
                .forEach { availableYear ->
                    CatalogChoiceRow(
                        label =
                            availableYear.toString(),
                        selected =
                            draftYear ==
                                    availableYear.toString(),
                        onClick = {
                            draftYear =
                                availableYear.toString()
                        }
                    )
                }

            CatalogSheetSectionTitle(
                title = "Nota mínima"
            )

            val ratingOptions =
                if (ratings.isEmpty()) {
                    listOf(
                        CatalogFilterOption(
                            "Qualquer",
                            ""
                        )
                    )
                } else {
                    ratings
                }

            ratingOptions.forEach { option ->
                CatalogChoiceRow(
                    label = option.label,
                    selected =
                        draftRating == option.value,
                    onClick = {
                        draftRating = option.value
                    }
                )
            }

            if (types.size > 1) {
                CatalogSheetSectionTitle(
                    title = typeTitle
                )

                types.forEach { option ->
                    CatalogChoiceRow(
                        label = option.label,
                        selected =
                            draftType == option.value,
                        onClick = {
                            draftType = option.value
                        }
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Button(
                onClick = {
                    onApply(
                        draftSearch,
                        draftYear,
                        draftRating,
                        draftType
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            LaranjadaOrange,
                        contentColor =
                            Color.Black
                    ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Aplicar filtros",
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(
                onClick = onClear,
                modifier = Modifier.align(
                    Alignment.CenterHorizontally
                )
            ) {
                Text(
                    text = "Limpar filtros",
                    color = Color.White
                )
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }
    }
}

@Composable
private fun CatalogSheetTitle(
    title: String,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        IconButton(
            onClick = onClose
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.Close,
                contentDescription = "Fechar",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun CatalogSheetSectionTitle(
    title: String
) {
    Spacer(
        modifier = Modifier.height(15.dp)
    )

    HorizontalDivider(
        color = Color.White.copy(
            alpha = 0.10f
        )
    )

    Spacer(
        modifier = Modifier.height(13.dp)
    )

    Text(
        text = title,
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )

    Spacer(
        modifier = Modifier.height(5.dp)
    )
}

@Composable
private fun CatalogChoiceRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            )
            .padding(vertical = 6.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors =
                RadioButtonDefaults.colors(
                    selectedColor =
                        LaranjadaOrange
                )
        )

        Text(
            text = label,
            color = Color.White,
            fontSize = 13.sp,
            textAlign = TextAlign.Start
        )
    }
}
