package com.muthuraj.cycle.fill.ui.recents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muthuraj.cycle.fill.util.compose.DaysElapsedChip

@Composable
fun RecentsScreen(
    screenState: RecentsScreenState,
    doAction: (RecentsScreenEvent) -> Unit
) {
    when (screenState) {
        is RecentsScreenState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = screenState.message)
            }
        }

        RecentsScreenState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is RecentsScreenState.Success -> {
            if (screenState.dates.categories.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recent entries",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier)
                    }
                    screenState.dates.categories.forEach { category ->
                        item {
                            CategorySection(category)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySection(category: RecentCategory) {
    if (category.subCategories.size == 1) {
        val subcategory = category.subCategories.first()

        if (subcategory.collections.size == 1) {
            val collection = subcategory.collections.first()

            GroupCard(
                groupName = "${category.name} - ${subcategory.name} - ${collection.name}",
                color = CombinedAllColor
            ) {
                collection.items.forEach { item ->
                    DateItem(
                        item = item,
                        number = item.number
                    )
                }
            }
        } else {
            GroupCard(
                groupName = "${category.name} - ${subcategory.name}",
                color = CombinedCategorySubColor
            ) {
                subcategory.collections.forEach { collection ->
                    CollectionSection(
                        collection = collection,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    } else {
        GroupCard(groupName = category.name, color = CategoryColor) {
            category.subCategories.forEach { subcategory ->
                SubcategorySection(
                    subcategory = subcategory,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun SubcategorySection(
    subcategory: RecentSubCategory,
    modifier: Modifier = Modifier
) {
    GroupCard(groupName = subcategory.name, color = SubCategoryColor) {
        // Collections
        subcategory.collections.forEach { collection ->
            CollectionSection(
                collection = collection,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
private fun CollectionSection(
    collection: RecentCollection,
    modifier: Modifier = Modifier
) {
    GroupCard(groupName = collection.name, color = CollectionColor) {
        Spacer(Modifier.size(4.dp))
        // Items
        collection.items.forEach { item ->
            DateItem(
                item = item,
                number = item.number
            )
        }
    }
}

@Composable
private fun DateItem(
    item: ItemDetailed,
    number: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f).fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ).align(Alignment.Top),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primary,
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.date,
                    style = MaterialTheme.typography.subtitle1
                )
                if (item.comment.isNullOrBlank().not()) {
                    Text(
                        text = item.comment!!,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = item.weekDay,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            item.daysAgoForLastCycle?.let { days ->
                if (item.daysAgoForLastCycle.second.isNotBlank()) {
                    DaysElapsedChip(
                        daysElapsed = days,
                        text = "${item.daysAgoForLastCycle.second} cycle"
                    )
                }
            }
        }
    }
}

@Composable
fun GroupCard(
    groupName: String,
    color: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Box {
        // Main card with rounded rectangle border
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .border(
                    width = Dp.Hairline,
                    color = color,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp) // Inner padding inside the card
        ) {
            content()
        }

        // Group name "overlapping" the border
        Text(
            text = " $groupName ",
            modifier = Modifier
                .padding(start = 16.dp)
                .background(
                    color = MaterialTheme.colors.background,
                    shape = RoundedCornerShape(8.dp)
                ),
            color = color,
            fontSize = 12.sp,
            textAlign = TextAlign.Start
        )
    }
}

val CategoryColor = Color(0xFF078507)
val SubCategoryColor = Color.Blue
val CollectionColor = Color(0xFF056E6E)
val CombinedCategorySubColor = Color.Magenta
val CombinedAllColor = Color.Red  // Combined Category+Subcategory+Collection
