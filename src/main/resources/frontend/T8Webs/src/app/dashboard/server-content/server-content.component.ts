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

@Component({
  selector: 'server-content',
  templateUrl: './server-content.component.html',
  styleUrls: ['./server-content.component.less']
})
export class ServerContentComponent implements OnInit, AfterViewChecked, OnChanges, OnDestroy {
  @ViewChild('serverDashboard') serverDashboard!: DashboardLayoutComponent;
  // Subscriptions
  private intervalSub: Subscription | undefined;

  @Input() selectedTreeNode = new TreeNode(-1, 'Dashboard', '', NodeType.None);
  @Output() showSpinner: EventEmitter<any> = new EventEmitter<any>();
  @Output() hideSpinner: EventEmitter<any> = new EventEmitter<any>();

  netInData: { x: Date, y: string }[] = [];
  netOutData: { x: Date, y: string }[] = [];
  cpuData: { x: Date, y: string }[] = [];
  ramData: { x: Date, y: string }[] = [];

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
    this.setServerData(response);
  }

  private setServerData(response: any) {
    const netInData: { x: Date, y: string }[] = [];
    const netOutData: { x: Date, y: string }[] = [];
    const cpuData: { x: Date, y: string }[] = [];
    const ramData: { x: Date, y: string }[] = [];

    if (response && response.data && Array.isArray(response.data)) {
      response.data.forEach((dataObj: any) => {
        const date = new Date(0);
        date.setUTCSeconds(dataObj.time - 240);

        const netIn = (dataObj.netin ?? 0).toFixed(2);
        const netOut = (dataObj.netout ?? 0).toFixed(2);
        const cpu = ((dataObj.cpu ?? 0) * 100).toFixed(2);
        const mem = (Number(dataObj.mem ?? 0) / 1048576).toFixed(2);

        netInData.push({x: date, y: netIn});
        netOutData.push({x: date, y: netOut});
        cpuData.push({x: date, y: cpu});
        ramData.push({x: date, y: mem});
      });
    }

    this.netInData = netInData;
    this.netOutData = netOutData;
    this.cpuData = cpuData;
    this.ramData = ramData;
  }
}
