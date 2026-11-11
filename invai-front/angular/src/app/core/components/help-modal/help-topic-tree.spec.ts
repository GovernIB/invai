import { HelpTopic } from './help-topic.model';
import { groupsContaining, toTopicNodes } from './help-topic-tree';

const INDEX: HelpTopic[] = [
  {
    label: 'Primers passos',
    items: [{ label: 'Ajuda', document: 'getting-started/using-help.md' }],
  },
  {
    label: 'Aplicacions',
    items: [
      { label: 'Llista', document: 'applications/list.md' },
      {
        label: 'Fitxa',
        items: [{ label: 'General', document: 'application-detail/general.md' }],
      },
    ],
  },
];

describe('toTopicNodes', () => {
  it('keys each entry by its position and records its depth and the id of its list', () => {
    const [, applications] = toTopicNodes(INDEX);
    const detail = applications.children[1];

    expect(applications).toMatchObject({ key: '1', level: 0, listId: 'help-topics-1' });
    expect(detail).toMatchObject({ key: '1-1', level: 1, listId: 'help-topics-1-1' });
    expect(detail.children[0]).toMatchObject({
      key: '1-1-0',
      level: 2,
      document: 'application-detail/general.md',
      children: [],
    });
  });
});

describe('groupsContaining', () => {
  const nodes = toTopicNodes(INDEX);

  it('returns the groups of a topic from the outermost', () => {
    expect(groupsContaining(nodes, 'application-detail/general.md')).toEqual(['1', '1-1']);
    expect(groupsContaining(nodes, 'getting-started/using-help.md')).toEqual(['0']);
  });

  it('returns nothing for a document the index does not list', () => {
    expect(groupsContaining(nodes, 'missing.md')).toEqual([]);
  });
});
