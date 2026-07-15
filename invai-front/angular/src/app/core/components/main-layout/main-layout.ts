import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Sidebar } from './components/sidebar/sidebar';
import { TopMenu } from './components/top-menu/top-menu';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-main-layout',
  imports: [Sidebar, TopMenu],
  templateUrl: './main-layout.html',
})
export class MainLayout {}
