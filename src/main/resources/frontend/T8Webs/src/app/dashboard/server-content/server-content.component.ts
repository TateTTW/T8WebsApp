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

  @Input() selectedTreeNode = new TreeNode(-1, 'Dashboard', '', NodeType.None);
  @Output() showSpinner: EventEmitter<any> = new EventEmitter<any>();
  @Output() hideSpinner: EventEmitter<any> = new EventEmitter<any>();

  netData: NetData = {unit: 'Kb', netIn: [], netOut: []};
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
    const threshold = 2048;
    const cpuData: { x: Date, y: string }[] = [];
    const ramData: { x: Date, y: string }[] = [];
    const netData: NetData = {unit: 'Kb', netIn: [], netOut: []};
    const netDataCompressed: {netIn: { x: Date, y: string }, netOut: { x: Date, y: string }}[] = [];

    if (response && response.data && Array.isArray(response.data)) {
      response.data.forEach((dataObj: any) => {
        const date = new Date(0);
        date.setUTCSeconds(dataObj.time - 240);

        const netIn = (dataObj.netin ?? 0).toFixed(2);
        const netOut = (dataObj.netout ?? 0).toFixed(2);
        const cpu = ((dataObj.cpu ?? 0) * 100).toFixed(2);
        const mem = (Number(dataObj.mem ?? 0) / 1048576).toFixed(2);

        if(netIn > threshold || netOut > threshold){
          netData.unit = 'Mb';
        }

        netDataCompressed.push({netIn: {x: date, y: netIn}, netOut: {x: date, y: netOut}})
        cpuData.push({x: date, y: cpu});
        ramData.push({x: date, y: mem});
      });
    }

    if(netData.unit == 'Mb'){
      netDataCompressed.forEach(data => {
        data.netIn.y = (parseFloat(data.netIn.y) / 1024).toFixed(2);
        data.netOut.y = (parseFloat(data.netOut.y) / 1024).toFixed(2);
        netData.netIn.push(data.netIn);
        netData.netOut.push(data.netOut);
      });
    } else {
      netDataCompressed.forEach(data => {
        netData.netIn.push(data.netIn);
        netData.netOut.push(data.netOut);
      });
    }

    this.netData = netData;
    this.cpuData = cpuData;
    this.ramData = ramData;
  }
}
