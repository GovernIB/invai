import { MessageService } from 'primeng/api';

import {
  SOFT_DELETE_RESTORE_PENDING_DETAIL,
  SOFT_DELETE_RESTORE_PENDING_SUMMARY,
} from '@shared/constants/soft-delete-status.constants';

export function showRestorePending(messageService: MessageService): void {
  messageService.add({
    severity: 'info',
    summary: SOFT_DELETE_RESTORE_PENDING_SUMMARY,
    detail: SOFT_DELETE_RESTORE_PENDING_DETAIL,
  });
}
