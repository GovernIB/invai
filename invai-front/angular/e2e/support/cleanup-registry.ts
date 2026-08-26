import type { TestInfo } from '@playwright/test';

interface CleanupTask {
  label: string;
  run: () => Promise<void>;
}

export class CleanupRegistry {
  private readonly tasks: CleanupTask[] = [];

  constructor(private readonly testInfo: TestInfo) {}

  register(label: string, run: () => Promise<void>): void {
    this.tasks.push({ label, run });
  }

  async runAll(): Promise<void> {
    if (this.tasks.length > 0) {
      this.testInfo.setTimeout(
        this.testInfo.timeout + Math.max(30_000, this.tasks.length * 15_000),
      );
    }

    const failures: Error[] = [];

    while (this.tasks.length > 0) {
      const task = this.tasks.pop()!;

      try {
        await task.run();
      } catch (error) {
        const failure = error instanceof Error ? error : new Error(String(error));
        failures.push(new Error(`${task.label}: ${failure.message}`, { cause: failure }));
        await this.testInfo.attach(`${attachmentName(task.label)}-cleanup-error`, {
          body: failure.stack ?? failure.message,
          contentType: 'text/plain',
        });
      }
    }

    if (failures.length > 0) {
      throw new AggregateError(failures, `${failures.length} E2E cleanup task(s) failed`);
    }
  }
}

function attachmentName(label: string): string {
  return label
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-|-$/g, '')
    .slice(0, 80);
}
