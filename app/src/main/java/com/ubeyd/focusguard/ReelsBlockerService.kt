package com.ubeyd.focusguard

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReelsBlockerService : AccessibilityService() {

    private val serviceScope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.Default
        )

    private lateinit var preferencesManager: PreferencesManager

    @Volatile
    private var blockInstagramReels = true

    @Volatile
    private var blockYouTubeShorts = true

    private var lastEventTime = 0L
    private var lastBackTime = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()

        preferencesManager =
            PreferencesManager(applicationContext)

        serviceScope.launch {
            preferencesManager.blockInstagramReels
                .collectLatest { enabled ->
                    blockInstagramReels = enabled
                }
        }

        serviceScope.launch {
            preferencesManager.blockYouTubeShorts
                .collectLatest { enabled ->
                    blockYouTubeShorts = enabled
                }
        }
    }

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {
        try {
            if (event == null) {
                return
            }

            val now = SystemClock.uptimeMillis()

            if (now - lastEventTime < EVENT_THROTTLE_MS) {
                return
            }

            lastEventTime = now

            val packageName =
                event.packageName ?: return

            val isInstagram =
                packageName == INSTAGRAM_PACKAGE

            val isYouTube =
                packageName == YOUTUBE_PACKAGE

            if (!isInstagram && !isYouTube) {
                return
            }

            if (
                isInstagram &&
                !blockInstagramReels
            ) {
                return
            }

            if (
                isYouTube &&
                !blockYouTubeShorts
            ) {
                return
            }

            if (
                now - lastBackTime <
                BACK_COOLDOWN_MS
            ) {
                return
            }

            val root =
                rootInActiveWindow ?: return

            if (isInstagram) {

                if (containsInstagramReels(root)) {
                    blockCurrentScreen(now)
                }

                return
            }

            if (isYouTube) {

                if (containsYouTubeShorts(root)) {
                    blockCurrentScreen(now)
                }
            }

        } catch (_: Throwable) {
            // Accessibility event hataları
            // servisi durdurmamalı.
        }
    }

    private fun blockCurrentScreen(
        now: Long
    ) {
        try {
            lastBackTime = now

            performGlobalAction(
                GLOBAL_ACTION_BACK
            )
        } catch (_: Throwable) {
            // Global BACK başarısız olabilir.
        }
    }

    private fun containsInstagramReels(
        root: AccessibilityNodeInfo
    ): Boolean {
        return try {

            if (
                hasAnyViewId(
                    root,
                    INSTAGRAM_REEL_VIEW_IDS
                )
            ) {
                true
            } else {
                shallowTextSearch(
                    node = root,
                    keywords =
                        INSTAGRAM_REEL_KEYWORDS,
                    depth = 0
                )
            }

        } catch (_: Throwable) {
            false
        }
    }

    private fun containsYouTubeShorts(
        root: AccessibilityNodeInfo
    ): Boolean {
        return try {

            if (
                hasAnyViewId(
                    root,
                    YOUTUBE_SHORTS_VIEW_IDS
                )
            ) {
                true
            } else {
                shallowTextSearch(
                    node = root,
                    keywords =
                        YOUTUBE_SHORTS_KEYWORDS,
                    depth = 0
                )
            }

        } catch (_: Throwable) {
            false
        }
    }

    private fun hasAnyViewId(
        root: AccessibilityNodeInfo,
        viewIds: Array<String>
    ): Boolean {

        for (viewId in viewIds) {
            try {
                val nodes =
                    root.findAccessibilityNodeInfosByViewId(
                        viewId
                    )

                if (!nodes.isNullOrEmpty()) {
                    return true
                }
            } catch (_: Throwable) {
                // Bazı cihazlarda View ID sorgusu
                // hata verebilir.
            }
        }

        return false
    }

    private fun shallowTextSearch(
        node: AccessibilityNodeInfo?,
        keywords: Array<String>,
        depth: Int
    ): Boolean {

        if (node == null) {
            return false
        }

        if (depth > MAX_TREE_DEPTH) {
            return false
        }

        try {

            val text =
                try {
                    node.text
                } catch (_: Throwable) {
                    null
                }

            if (text != null) {
                if (
                    containsKeyword(
                        text.toString(),
                        keywords
                    )
                ) {
                    return true
                }
            }

            val description =
                try {
                    node.contentDescription
                } catch (_: Throwable) {
                    null
                }

            if (description != null) {
                if (
                    containsKeyword(
                        description.toString(),
                        keywords
                    )
                ) {
                    return true
                }
            }

            val childCount =
                try {
                    node.childCount
                } catch (_: Throwable) {
                    0
                }

            if (childCount <= 0) {
                return false
            }

            var index = 0

            while (index < childCount) {

                val child =
                    try {
                        node.getChild(index)
                    } catch (_: Throwable) {
                        null
                    }

                if (child != null) {

                    val found =
                        try {
                            shallowTextSearch(
                                node = child,
                                keywords = keywords,
                                depth = depth + 1
                            )
                        } catch (_: Throwable) {
                            false
                        }

                    try {
                        child.recycle()
                    } catch (_: Throwable) {
                        // No-op.
                    }

                    if (found) {
                        return true
                    }
                }

                index++
            }

        } catch (_: Throwable) {
            return false
        }

        return false
    }

    private fun containsKeyword(
        value: String,
        keywords: Array<String>
    ): Boolean {

        for (keyword in keywords) {
            try {
                if (
                    value.contains(
                        other = keyword,
                        ignoreCase = true
                    )
                ) {
                    return true
                }
            } catch (_: Throwable) {
                // Hatalı accessibility text'i atla.
            }
        }

        return false
    }

    override fun onInterrupt() {
        // Servis kesildiğinde özel işlem gerekmiyor.
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {

        private const val EVENT_THROTTLE_MS = 200L

        private const val BACK_COOLDOWN_MS = 500L

        private const val MAX_TREE_DEPTH = 5

        private const val INSTAGRAM_PACKAGE =
            "com.instagram.android"

        private const val YOUTUBE_PACKAGE =
            "com.google.android.youtube"

        private val INSTAGRAM_REEL_VIEW_IDS =
            arrayOf(
                "com.instagram.android:id/clips_viewer_view_pager",
                "com.instagram.android:id/reel_viewer",
                "com.instagram.android:id/reel_viewer_media_container",
                "com.instagram.android:id/clips_video_container",
                "com.instagram.android:id/clips_tab"
            )

        private val YOUTUBE_SHORTS_VIEW_IDS =
            arrayOf(
                "com.google.android.youtube:id/reel_player_page_container",
                "com.google.android.youtube:id/shorts_player_page_container",
                "com.google.android.youtube:id/shorts_player",
                "com.google.android.youtube:id/reel_player"
            )

        private val INSTAGRAM_REEL_KEYWORDS =
            arrayOf(
                "reels",
                "reel",
                "reels video",
                "reels viewer",
                "reels feed",
                "reels tab"
            )

        private val YOUTUBE_SHORTS_KEYWORDS =
            arrayOf(
                "shorts",
                "youtube shorts",
                "shorts video",
                "shorts player"
            )
    }
}
