import { HelpTopic } from './help-topic.model';

/** An entry of the topic index as the dialog shows it: a group with children, or a topic. */
export interface TopicNode {
  /** Position in the index (`0`, `0-1`…): unique and stable while the index does not change. */
  key: string;
  label: string;
  icon?: string;
  document?: string;
  /** Depth in the index, from 0, for the indentation. */
  level: number;
  /** `id` of the list of its children, which its button controls. */
  listId: string;
  children: TopicNode[];
}

export function toTopicNodes(topics: readonly HelpTopic[], parentKey = '', level = 0): TopicNode[] {
  return topics.map((topic, index) => {
    const key = parentKey ? `${parentKey}-${index}` : `${index}`;
    return {
      key,
      label: topic.label,
      icon: topic.icon,
      document: topic.document,
      level,
      listId: `help-topics-${key}`,
      children: toTopicNodes(topic.items ?? [], key, level + 1),
    };
  });
}

/** Keys of the groups that contain `document`, from the outermost; empty if it is not listed. */
export function groupsContaining(nodes: readonly TopicNode[], document: string): string[] {
  const group = nodes.find((node) => contains(node, document));
  return group ? [group.key, ...groupsContaining(group.children, document)] : [];
}

function contains(node: TopicNode, document: string): boolean {
  return node.children.some((child) => child.document === document || contains(child, document));
}
