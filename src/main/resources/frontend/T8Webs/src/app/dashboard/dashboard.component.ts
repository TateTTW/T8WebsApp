import {AfterViewInit, Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {User} from "./dto/user";
import {NodeType, TreeNode} from "./dashboard-tree/TreeNode";
import {createSpinner, hideSpinner, showSpinner} from "@syncfusion/ej2-angular-popups";
import {Job, JobAction} from "./server-dialog/Job";
import {ServerDialogComponent} from "./server-dialog/server-dialog.component";

@Component({
  selector: 'dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.less']
})
export class DashboardComponent implements OnInit, AfterViewInit, OnDestroy {
  // View Elements
  @ViewChild ('serverDialog') serverDialog!: ServerDialogComponent

  user: User | undefined;
  selectedTreeNode = new TreeNode(-1, 'Dashboard', NodeType.None);

  constructor() { }

  ngOnInit(): void { }

  ngOnDestroy(): void { }

  setUser(user: User) {
    this.user = user;
  }

  nodeSelection(treeNode: TreeNode) {
    this.selectedTreeNode = treeNode;
  }

  ngAfterViewInit(): void {
    setTimeout((()=>{
      const spinnerTarget = document.querySelector('#spinnerDiv') as HTMLElement;
      createSpinner({ target:spinnerTarget, width: '70px' });
    }).bind(this), 300);
  }

  showSpinner(): void {
    const spinnerTarget = document.querySelector('#spinnerDiv') as HTMLElement;
    showSpinner(spinnerTarget);
  }

  hideSpinner(): void {
    const spinnerTarget = document.querySelector('#spinnerDiv') as HTMLElement;
    hideSpinner(spinnerTarget);
  }

  openServerDialog(job: Job) {
    this.serverDialog.job = job;
    this.serverDialog.dialog.show()
  }

  processJob(job: Job) {
    if(job.action == JobAction.Add || job.action == JobAction.Rename){
      this.openServerDialog(job);
    }
  }
}
