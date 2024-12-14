package com.muthuraj.cycle.fill.ui.recents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Recent Entries",
                            style = MaterialTheme.typography.h6,
                        )
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
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Category Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        MaterialTheme.colors.primary,
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.primary
            )
        }

        // Subcategories
        category.subCategories.forEach { subcategory ->
            SubcategorySection(
                subcategory = subcategory,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
private fun SubcategorySection(
    subcategory: RecentSubCategory,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Subcategory Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(
                        MaterialTheme.colors.secondary,
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = subcategory.name,
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.secondary
            )
        }

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
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Collection Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(3.dp)
                    .background(
                        MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = collection.name,
                style = MaterialTheme.typography.subtitle2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        }

        // Items
        collection.items.forEach {item ->
            DateItem(
                item = item,
                number = item.number,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
private fun DateItem(
    item: ItemDetailed,
    number: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                        color = MaterialTheme.colors.primary
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
                        DaysElapsedChip(daysElapsed = days, text = "${item.daysAgoForLastCycle.second} cycle")
                    }
                }
            }
        }
    }
} 