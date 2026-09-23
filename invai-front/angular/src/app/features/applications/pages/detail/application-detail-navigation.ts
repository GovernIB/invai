/** A one-navigation intent; never persist edit mode in the URL or history state. */
export interface ApplicationDetailNavigationInfo {
  mode: 'edit';
  section: 'general';
}

export const APPLICATION_GENERAL_EDIT_NAVIGATION: ApplicationDetailNavigationInfo = {
  mode: 'edit',
  section: 'general',
};

export function isApplicationGeneralEditNavigation(
  info: unknown,
): info is ApplicationDetailNavigationInfo {
  return (
    typeof info === 'object' && info !== null &&
    'mode' in info && info.mode === 'edit' &&
    'section' in info && info.section === 'general'
  );
}
