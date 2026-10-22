import { ValidationStatus } from './validation-status.model';

export type Dir3ValidationStatus = ValidationStatus;

export interface Dir3Validation {
  id: number;
  dir3Status: Dir3ValidationStatus;
  reason: string | null;
  manualValidatedAt: string | null;
  manualValidatedBy: string | null;
}

export interface PersonDir3Check {
  personGroup: string | null;
  groupDir3: string | null;
  admUnitCode: string;
  matches: boolean;
}
