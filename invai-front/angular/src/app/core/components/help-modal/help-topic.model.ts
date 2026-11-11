/**
 * Entry of `public/help/<locale>/index.json`. A topic either opens a document, groups
 * other topics, or both. Paths are relative to the locale folder.
 */
export interface HelpTopic {
  label: string;
  icon?: string;
  document?: string;
  items?: HelpTopic[];
}

export type HelpLocale = 'ca' | 'es';

/** Plain text around the first match, split so the match can be marked. */
export interface SearchExcerpt {
  before: string;
  match: string;
  after: string;
}

/** A document of the index with the groups that contain it, from the outermost. */
export interface HelpDocument {
  label: string;
  document: string;
  trail: string[];
}

export interface SearchResult extends HelpDocument {
  /** Accessible name of the option: the title and, when it has them, its groups. */
  name: string;
  excerpt: SearchExcerpt;
}
