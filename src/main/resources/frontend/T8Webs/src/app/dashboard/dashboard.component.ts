import {Component, OnDestroy, OnInit} from '@angular/core';
import {DashboardService} from "./dashboard.service";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.less']
})
export class DashboardComponent implements OnInit, OnDestroy {
  private assignServerSub: Subscription | undefined;

  constructor(private dashboardService: DashboardService) { }

  ngOnInit(): void { }

  ngOnDestroy(): void {
    this.assignServerSub?.unsubscribe();
  }

  assignServer(): void {
    this.assignServerSub = this.dashboardService.assignServer("testName").subscribe(
      data => console.log(data),
      error => console.log(error)
    )
  }

}
