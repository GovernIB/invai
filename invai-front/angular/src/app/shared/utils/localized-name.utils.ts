export interface LocalizedName {
  id: number;
  name: string | null;
  nameEs?: string | null;
}

export function localizedName(
  item: LocalizedName,
  locale: string,
  fallback?: string | null,
): string {
  const primaryName = locale.toLowerCase().startsWith('es') ? item.nameEs : item.name;
  const secondaryName = locale.toLowerCase().startsWith('es') ? item.name : item.nameEs;

  return primaryName?.trim() || secondaryName?.trim() || fallback?.trim() || String(item.id);
}
