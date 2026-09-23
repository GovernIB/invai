import { ApplicationResponsibleOutput } from './applications.model';

/** Matches the orange DIR3 indicator on active CAIB responsible assignments. */
export function hasPendingResponsibleDir3(
  assignments: readonly ApplicationResponsibleOutput[],
): boolean {
  return assignments.some(
    (assignment) =>
      assignment.person?.personalCaib &&
      !assignment.deletedAt &&
      assignment.dir3Validation?.dir3Status === 'NOT_VALIDATED',
  );
}
