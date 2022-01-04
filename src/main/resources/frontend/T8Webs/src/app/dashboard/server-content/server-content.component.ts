import {Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {UploaderComponent} from "@syncfusion/ej2-angular-inputs";
import {DashboardService} from "../dashboard.service";
import {Subscription} from "rxjs";

@Component({
  selector: 'server-content',
  templateUrl: './server-content.component.html',
  styleUrls: ['./server-content.component.less']
})
export class ServerContentComponent implements OnInit, OnDestroy {
  // View Elements
  @ViewChild ('uploader') uploader!: UploaderComponent;
  // Subscriptions
  private deployBuildSub: Subscription | undefined;

  constructor(private dashboardService: DashboardService) { }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.deployBuildSub?.unsubscribe();
  }

  deploy() {
    let file: File = <File>this.uploader.getFilesData(0)[0].rawFile;
    this.deployBuildSub = this.dashboardService.deployBuild(128, file).subscribe(
      data => console.log(data),
      error => console.log(error)
    )
  }

}
