package com.jerboa.ui.components.common

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.jerboa.R
import com.jerboa.datatypes.PersonSafe
import com.jerboa.datatypes.api.GetUnreadCountResponse
import com.jerboa.datatypes.samplePersonSafe
import com.jerboa.datatypes.samplePost
import com.jerboa.db.Account
import com.jerboa.loginFirstToast
import com.jerboa.siFormat
import com.jerboa.ui.components.person.PersonProfileLink
import com.jerboa.ui.theme.*
import com.jerboa.unreadCountTotal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopAppBar(
    text: String,
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Text(
                text = text,
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                )
            }
        },
    )
}

@Composable
fun BottomAppBarAll(
    navController: NavController = rememberNavController(),
    screen: String,
    unreadCounts: GetUnreadCountResponse? = null,
    showBottomNav: Boolean? = true,
    onClickSaved: () -> Unit,
    onClickProfile: () -> Unit,
    onClickInbox: () -> Unit,
) {
    val totalUnreads = unreadCounts?.let { unreadCountTotal(it) }

    if (showBottomNav == true) {
        val window = (LocalContext.current as Activity).window
        val colorScheme = MaterialTheme.colorScheme

        DisposableEffect(Unit) {
            window.navigationBarColor = colorScheme.surfaceColorAtElevation(3.dp).toArgb()

            onDispose {
                window.navigationBarColor = colorScheme.background.toArgb()
            }
        }

        BottomAppBar {
            NavigationBarItem(
                icon = {
                    if (screen == "home") {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "TODO",
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Home,
                            contentDescription = "TODO",
                        )
                    }
                },
                selected = false,
                onClick = {
                    navController.navigate("home")
                },
            )

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.List,
                        contentDescription = "TODO",
                    )
                },
                onClick = {
                    navController.navigate("communityList")
                },
                selected = screen == "communityList",
            )
            NavigationBarItem(
                icon = {
                    if (screen == "inbox") {
                        InboxIconAndBadge(
                            iconBadgeCount = totalUnreads,
                            icon = Icons.Filled.Email,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        InboxIconAndBadge(
                            iconBadgeCount = totalUnreads,
                            icon = Icons.Outlined.Email,
                        )
                    }
                },
                onClick = {
                    onClickInbox()
                },
                selected = false,
            )
            NavigationBarItem(
                icon = {
                    if (screen == "saved") {
                        Icon(
                            imageVector = Icons.Filled.Bookmarks,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "TODO",
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Bookmarks,
                            contentDescription = "TODO",
                        )
                    }
                },
                onClick = {
                    onClickSaved()
                },
                selected = false,
            )
            NavigationBarItem(
                icon = {
                    if (screen == "profile") {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "TODO",
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "TODO",
                        )
                    }
                },
                onClick = onClickProfile,
                selected = false,
            )
        }
    }
}

@Preview
@Composable
fun BottomAppBarAllPreview() {
    BottomAppBarAll(
        onClickInbox = {},
        onClickProfile = {},
        onClickSaved = {},
        screen = "home",
        showBottomNav = true,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommentOrPostNodeHeader(
    creator: PersonSafe,
    score: Int,
    myVote: Int?,
    published: String,
    updated: String?,
    deleted: Boolean,
    onPersonClick: (personId: Int) -> Unit,
    isPostCreator: Boolean,
    isModerator: Boolean,
    isCommunityBanned: Boolean,
    onClick: () -> Unit,
    onLongCLick: () -> Unit,
    isExpanded: Boolean = true,
    collapsedCommentsCount: Int = 0,
) {
    FlowRow(
        mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween,
        crossAxisAlignment = FlowCrossAxisAlignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = LARGE_PADDING,
                bottom = MEDIUM_PADDING,
            )
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onLongClick = onLongCLick,
                onClick = onClick,
            ),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(SMALL_PADDING),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (deleted) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "TODO",
                    tint = MaterialTheme.colorScheme.error,
                )
                DotSpacer(style = MaterialTheme.typography.bodyMedium)
            }

            PersonProfileLink(
                person = creator,
                onClick = { onPersonClick(creator.id) },
                showTags = true,
                isPostCreator = isPostCreator,
                isModerator = isModerator,
                isCommunityBanned = isCommunityBanned,
            )
        }
        ScoreAndTime(
            score = score,
            myVote = myVote,
            published = published,
            updated = updated,
            isExpanded = isExpanded,
            collapsedCommentsCount = collapsedCommentsCount,
        )
    }
}

@Preview
@Composable
fun CommentOrPostNodeHeaderPreview() {
    CommentOrPostNodeHeader(
        creator = samplePersonSafe,
        score = 23,
        myVote = 1,
        published = samplePost.published,
        updated = samplePost.updated,
        deleted = false,
        onPersonClick = {},
        isPostCreator = true,
        isModerator = true,
        isCommunityBanned = false,
        onClick = {},
        onLongCLick = {},
    )
}

@Composable
fun ActionBarButton(
    onClick: () -> Unit,
    icon: ImageVector,
    text: String? = null,
    contentColor: Color = MaterialTheme.colorScheme.onBackground.muted,
    noClick: Boolean = false,
    account: Account?,
    requiresAccount: Boolean = true,
) {
    val ctx = LocalContext.current
//    Button(
//        onClick = onClick,
//        colors = ButtonDefaults.buttonColors(
//            backgroundColor = Color.Transparent,
//            contentColor = contentColor,
//        ),
//        shape = MaterialTheme.shapes.large,
//        contentPadding = PaddingValues(SMALL_PADDING),
//        elevation = null,
//        content = content,
//        modifier = Modifier
//            .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
//    )
    val barMod = if (noClick) {
        Modifier
    } else {
        Modifier.clickable(onClick = {
            if (!requiresAccount || account !== null) {
                onClick()
            } else {
                loginFirstToast(ctx)
            }
        })
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = barMod,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "TODO",
            tint = contentColor,
        )
        text?.also {
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun DotSpacer(
    padding: Dp = SMALL_PADDING,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Text(
        text = stringResource(R.string.app_bars_dot_spacer),
        style = style,
        color = MaterialTheme.colorScheme.onBackground.muted,
        modifier = Modifier.padding(horizontal = padding),
    )
}

@Composable
fun scoreColor(myVote: Int?): Color {
    return when (myVote) {
        1 -> MaterialTheme.colorScheme.secondary
        -1 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onBackground.muted
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxIconAndBadge(
    iconBadgeCount: Int?,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    if (iconBadgeCount !== null && iconBadgeCount > 0) {
        BadgedBox(
            modifier = modifier,
            badge = {
                Badge(
                    content = {
                        Text(
                            text = iconBadgeCount.toString(),
                        )
                    },
                )
            },
            content = {
                Icon(
                    imageVector = icon,
                    contentDescription = "TODO",
                    tint = tint,
                )
            },
        )
    } else {
        Icon(
            imageVector = icon,
            contentDescription = "TODO",
            tint = tint,
            modifier = modifier,
        )
    }
}

@Composable
fun Sidebar(
    title: String?,
    banner: String?,
    icon: String?,
    content: String?,
    published: String,
    postCount: Int,
    commentCount: Int,
    usersActiveDay: Int,
    usersActiveWeek: Int,
    usersActiveMonth: Int,
    usersActiveHalfYear: Int,
    padding: PaddingValues,
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .padding(padding)
            .simpleVerticalScrollbar(listState),
        verticalArrangement = Arrangement.spacedBy(MEDIUM_PADDING),
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomStart,
            ) {
                banner?.also {
                    PictrsBannerImage(
                        url = it,
                        modifier = Modifier.height(PROFILE_BANNER_SIZE),
                    )
                }
                Box(modifier = Modifier.padding(MEDIUM_PADDING)) {
                    icon?.also {
                        LargerCircularIcon(icon = it)
                    }
                }
            }
        }
        item {
            Column(
                modifier = Modifier.padding(MEDIUM_PADDING),
                verticalArrangement = Arrangement.spacedBy(MEDIUM_PADDING),
            ) {
                title?.also {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                TimeAgo(
                    precedingString = stringResource(R.string.AppBars_created),
                    includeAgo = true,
                    published = published,
                )
                CommentsAndPosts(
                    usersActiveDay = usersActiveDay,
                    usersActiveWeek = usersActiveWeek,
                    usersActiveMonth = usersActiveMonth,
                    usersActiveHalfYear = usersActiveHalfYear,
                    postCount = postCount,
                    commentCount = commentCount,
                )
            }
        }
        item {
            Divider()
        }
        item {
            content?.also {
                Column(
                    modifier = Modifier.padding(MEDIUM_PADDING),
                ) {
                    MyMarkdownText(
                        markdown = it,
                        color = MaterialTheme.colorScheme.onBackground.muted,
                        onClick = {},
                    )
                }
            }
        }
    }
}

@Composable
fun CommentsAndPosts(
    usersActiveDay: Int,
    usersActiveWeek: Int,
    usersActiveMonth: Int,
    usersActiveHalfYear: Int,
    postCount: Int,
    commentCount: Int,
) {
    FlowRow {
        Text(
            text = stringResource(R.string.AppBars_users_day, siFormat(usersActiveDay)),
            color = MaterialTheme.colorScheme.onBackground.muted,
        )
        DotSpacer(style = MaterialTheme.typography.bodyMedium)
        Text(
            text = stringResource(R.string.AppBars_users_week, siFormat(usersActiveWeek)),
            color = MaterialTheme.colorScheme.onBackground.muted,
        )
        DotSpacer(style = MaterialTheme.typography.bodyMedium)
        Text(
            text = stringResource(R.string.AppBars_users_month, siFormat(usersActiveMonth)),
            color = MaterialTheme.colorScheme.onBackground.muted,
        )
        DotSpacer(style = MaterialTheme.typography.bodyMedium)
        Text(
            text = stringResource(R.string.AppBars_users_6_months, siFormat(usersActiveHalfYear)),
            color = MaterialTheme.colorScheme.onBackground.muted,
        )
        DotSpacer(style = MaterialTheme.typography.bodyMedium)
        Text(
            text = stringResource(R.string.AppBars_posts, siFormat(postCount)),
            color = MaterialTheme.colorScheme.onBackground.muted,
        )
        DotSpacer(style = MaterialTheme.typography.bodyMedium)
        Text(
            text = stringResource(R.string.AppBars_comments, siFormat(commentCount)),
            color = MaterialTheme.colorScheme.onBackground.muted,
        )
    }
}

@SuppressLint("ComposableModifierFactory")
@Composable
fun Modifier.simpleVerticalScrollbar(
    state: LazyListState,
    width: Dp = 4.dp,
): Modifier {
    val targetAlpha = if (state.isScrollInProgress) 0.5f else 0f
    val duration = if (state.isScrollInProgress) 150 else 500
    val color = MaterialTheme.colorScheme.onBackground

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration),
    )

    return drawWithContent {
        drawContent()

        val firstVisibleElementIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index
        val needDrawScrollbar = state.isScrollInProgress || alpha > 0.0f

        // Draw scrollbar if scrolling or if the animation is still running and lazy column has content
        if (needDrawScrollbar && firstVisibleElementIndex != null) {
            val elementHeight = this.size.height / state.layoutInfo.totalItemsCount
            val scrollbarOffsetY = firstVisibleElementIndex * elementHeight
            val scrollbarHeight = state.layoutInfo.visibleItemsInfo.size * elementHeight

            drawRect(
                color = color,
                topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
                size = Size(width.toPx(), scrollbarHeight),
                alpha = alpha,
            )
        }
    }
}
