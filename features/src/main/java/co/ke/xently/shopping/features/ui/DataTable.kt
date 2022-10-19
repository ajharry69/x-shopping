package co.ke.xently.shopping.features.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

object DataTable {
    val cacheWeightSaver = mapSaver(
        save = {
            buildMap {
                it.forEach { entry ->
                    this[entry.key.toString()] = entry.value
                }
            }
        },
        restore = {
            buildMap {
                it.forEach { entry ->
                    this[entry.key.toInt()] = entry.value.toString().toFloat()
                }
            }.toMutableMap()
        }
    )

    val boldTextContent: @Composable (Cell, Modifier) -> Unit = { cell, modifier ->
        cell(modifier) {
            Text(
                text = cell.label,
                textAlign = cell.textAlign,
                fontWeight = FontWeight.Bold,
                modifier = Cell.Content.modifier,
            )
        }
    }

    data class Cell(
        val label: String,
        val widthPercent: Number = 0f,
        val textAlign: TextAlign? = null,
        val onClick: () -> Unit = {},
        val content: (@Composable (Cell, Modifier) -> Unit) = { cell, modifier ->
            cell(modifier)
        },
    ) {
        @Composable
        operator fun invoke(
            modifier: Modifier,
            content: @Composable () -> Unit = {
                Content(text = label, textAlign = textAlign)
            },
        ) {
            Surface(
                modifier = modifier,
                onClick = onClick,
                content = content,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
            )
        }

        open class Content {
            companion object : Content() {
                val modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            }

            @Composable
            operator fun invoke(text: String, textAlign: TextAlign?) {
                Text(text = text, textAlign = textAlign, modifier = modifier)
            }
        }
    }

    @Composable
    fun Record(
        cells: List<Cell>,
        modifier: Modifier = Modifier,
        cacheWeight: MutableMap<Int, Float> = mutableMapOf(),
    ) {
        Row(modifier = modifier.height(IntrinsicSize.Min)) {
            val numberOfCells = cells.size
            val cleanedCells = if (cacheWeight.size < numberOfCells) {
                val unweightedCells = mutableMapOf<Int, Cell>()
                var occupiedPercentage = 0f
                cells.mapIndexed { index, cell ->
                    if (cell.widthPercent == 0f) {
                        unweightedCells[index] = cell
                    }
                    cell.also {
                        occupiedPercentage += it.widthPercent.toFloat()
                    }
                }.mapIndexed { index, cell ->
                    (unweightedCells[index]?.copy(widthPercent = (1 / unweightedCells.size.toFloat()) * (100 - occupiedPercentage))
                        ?: cell).also {
                        cacheWeight[index] = (it.widthPercent.toFloat() / 100) * numberOfCells
                    }
                }
            } else {
                cells
            }
            for ((index, cell) in cleanedCells.withIndex()) {
                cell.content(
                    cell,
                    Modifier
                        .weight(cacheWeight[index]!!)
                        .fillMaxHeight(),
                )
            }
        }
    }
}