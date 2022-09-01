import {
  AfterViewChecked,
  Component,
  EventEmitter,
  Input, OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import {DashboardService} from "../dashboard.service";
import {Subscription} from "rxjs";
import {interval} from 'rxjs';
import {NodeType, TreeNode} from "../dashboard-tree/TreeNode";
import {DashboardLayoutComponent} from "@syncfusion/ej2-angular-layouts";
import {NetData} from "./content/net-graph/net-graph.component";

@Component({
  selector: 'server-content',
  templateUrl: './server-content.component.html',
  styleUrls: ['./server-content.component.less']
})
export class ServerContentComponent implements OnInit, AfterViewChecked, OnChanges, OnDestroy {
  @ViewChild('serverDashboard') serverDashboard!: DashboardLayoutComponent;
  // Subscriptions
  private intervalSub: Subscription | undefined;

  @Input() selectedTreeNode = new TreeNode(-1, 'Dashboard', '', NodeType.NONE);
  @Output() showSpinner: EventEmitter<any> = new EventEmitter<any>();
  @Output() hideSpinner: EventEmitter<any> = new EventEmitter<any>();

  netData: NetData = {unit: 'Kb', netIn: [], netOut: []};
  cpuData: { x: Date, y: string }[] = [];
  ramData: { x: Date, y: string }[] = [];
  maxRam = 0;

  public mediaQuery: string = 'max-width: 1230px';
  public draggableHandle: string = ".e-panel-header";

  loaded = false;

  constructor(private dashboardService: DashboardService) { }

  ngOnInit(): void {
  }

  ngAfterViewChecked(): void {
    this.loaded = true;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if(changes['selectedTreeNode']){
      this.intervalSub?.unsubscribe();
      this.intervalSub = interval(30000).subscribe(() => {
        this.getServerData();
      });
      this.getServerData();
    }
  }

  ngOnDestroy(): void {
    this.intervalSub?.unsubscribe();
  }

  private async getServerData() {
    const response = await this.dashboardService.getServerData(this.selectedTreeNode.id).toPromise();
    const data = this.dashboardService.mapServerData(response);
    this.netData = data.netData;
    this.cpuData = data.cpuData;
    this.ramData = data.ramData;
    this.maxRam  = data.maxRam;
  }
}
