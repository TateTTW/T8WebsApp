import {AfterViewChecked, Component, EventEmitter, OnDestroy, OnInit, Output, ViewChild} from '@angular/core';
import {DashboardService} from "../dashboard.service";
import {NetData} from "../server-content/content/net-graph/net-graph.component";
import {interval, Subscription} from "rxjs";

@Component({
  selector: 'admin-servers',
  templateUrl: './admin-servers.component.html',
  styleUrls: ['./admin-servers.component.less']
})
export class AdminServersComponent implements OnInit, AfterViewChecked, OnDestroy {
  // Subscriptions
  private intervalSub: Subscription | undefined;

  @Output() refreshTree: EventEmitter<any> = new EventEmitter<any>();

  netData: NetData = {unit: 'Kb', netIn: [], netOut: []};
  cpuData: { x: Date, y: string }[] = [];
  ramData: { x: Date, y: string }[] = [];
  maxRam = 0;

  public mediaQuery: string = 'max-width: 1230px';
  public draggableHandle: string = ".e-panel-header";

  loaded = false;

  constructor(private dashboardService: DashboardService) { }

  ngOnInit(): void {
    this.trackSystemData({});
  }

  ngAfterViewChecked(): void {
    this.loaded = true;
  }

  ngOnDestroy(): void {
    this.intervalSub?.unsubscribe();
  }

  haltServerData(event: any) {
    this.intervalSub?.unsubscribe();
    this.netData = {unit: 'Kb', netIn: [], netOut: []};
    this.cpuData = [];
    this.ramData = [];
  }

  trackServerData(event: any) {
    if (event?.data?.vmid) {
      this.intervalSub?.unsubscribe();
      this.intervalSub = interval(30000).subscribe(() => {
        this.getServerData(event.data.vmid, event.data.userId);
      });
      this.getServerData(event.data.vmid, event.data.userId);
    }
  }

  trackSystemData(event: any) {
    this.intervalSub?.unsubscribe();
    this.intervalSub = interval(30000).subscribe(() => {
      this.getSystemData();
    });
    this.getSystemData();
  }

  private async getServerData(vmid: number, userId: string) {
    const response = await this.dashboardService.getUsersServerData(vmid, userId).toPromise();
    const data = this.dashboardService.mapServerData(response);
    this.netData = data.netData;
    this.cpuData = data.cpuData;
    this.ramData = data.ramData;
    this.maxRam = data.maxRam
  }

  private async getSystemData() {
    const response = await this.dashboardService.getSystemData().toPromise();
    const data = this.dashboardService.mapServerData(response);
    this.netData = data.netData;
    this.cpuData = data.cpuData;
    this.ramData = data.ramData;
    this.maxRam = data.maxRam;
  }
}
