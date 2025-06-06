/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.protonmail.android.mailnotifications.domain.usecase

import androidx.core.app.NotificationManagerCompat
import ch.protonmail.android.mailnotifications.data.repository.NotificationPermissionRepository
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

class ShouldShowNotificationPermissionDialog @Inject constructor(
    private val notificationManagerCompat: NotificationManagerCompat,
    private val notificationPermissionRepository: NotificationPermissionRepository
) {

    suspend operator fun invoke(currentTimeMillis: Long, isMessageSent: Boolean): Boolean {
        return notificationManagerCompat.areNotificationsEnabled().not() &&
            (
                wasDialogNeverShown() ||
                    isTwentyDaysSinceFirstShown(currentTimeMillis) &&
                    shouldStopShowingPermissionDialog().not() &&
                    isMessageSent
                )
    }

    private suspend fun wasDialogNeverShown() =
        notificationPermissionRepository.getNotificationPermissionTimestamp().isLeft()

    private suspend fun isTwentyDaysSinceFirstShown(currentTimeMillis: Long) =
        notificationPermissionRepository.getNotificationPermissionTimestamp().isRight {
            currentTimeMillis - it >= 20.days.inWholeMilliseconds
        }

    private suspend fun shouldStopShowingPermissionDialog() =
        notificationPermissionRepository.getShouldStopShowingPermissionDialog().getOrNull() ?: false
}
