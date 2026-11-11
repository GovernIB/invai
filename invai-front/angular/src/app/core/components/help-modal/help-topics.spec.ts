import { ActivatedRouteSnapshot } from '@angular/router';

import catalanIndex from '../../../../../public/help/ca/index.json';
import spanishIndex from '../../../../../public/help/es/index.json';
import { flattenTopics } from './help-content.service';
import { HelpTopic } from './help-topic.model';
import { HELP_TOPICS, helpTopic, routeHelpTopic } from './help-topics';

const documentsOf = (index: HelpTopic[]) =>
  flattenTopics(index).flatMap((topic) => (topic.document ? [topic.document] : []));

/** A chain of active routes, from the root to the deepest child, with the given `data`. */
function routeChain(...data: Record<string, unknown>[]): ActivatedRouteSnapshot {
  const routes = data.map((routeData) => ({ data: routeData, firstChild: null }));
  routes.forEach((route, index) => {
    (route as { firstChild: unknown }).firstChild = routes[index + 1] ?? null;
  });
  return routes[0] as unknown as ActivatedRouteSnapshot;
}

describe('HELP_TOPICS', () => {
  it('only points to documents that both help indexes list', () => {
    const topics = Object.values(HELP_TOPICS);

    expect(documentsOf(catalanIndex)).toEqual(expect.arrayContaining(topics));
    expect(documentsOf(spanishIndex)).toEqual(expect.arrayContaining(topics));
  });
});

describe('routeHelpTopic', () => {
  it('returns the document of the deepest route that declares one', () => {
    const root = routeChain({}, helpTopic(HELP_TOPICS.applicationsList), {
      breadcrumb: 'Afegir aplicació',
      ...helpTopic(HELP_TOPICS.applicationCreate),
    });

    expect(routeHelpTopic(root)).toBe(HELP_TOPICS.applicationCreate);
  });

  it('falls back to the document of the screen that contains a child without one', () => {
    const root = routeChain({}, helpTopic(HELP_TOPICS.applicationsList), { breadcrumb: 'General' });

    expect(routeHelpTopic(root)).toBe(HELP_TOPICS.applicationsList);
  });

  it('returns nothing when no active route declares a document', () => {
    expect(routeHelpTopic(routeChain({}, { breadcrumb: 'Detall' }))).toBeUndefined();
  });
});
