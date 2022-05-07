import {Component, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild} from '@angular/core';
import {DialogUtility} from "@syncfusion/ej2-popups";
import {NodeType, TreeNode} from "../../../dashboard-tree/TreeNode";
import {UploaderComponent} from "@syncfusion/ej2-angular-inputs";
import {Subscription} from "rxjs";
import {DashboardService} from "../../../dashboard.service";

@Component({
  selector: 'file-uploader',
  templateUrl: './file-uploader.component.html',
  styleUrls: ['./file-uploader.component.less']
})
export class FileUploaderComponent implements OnInit, OnDestroy {
  // View Elements
  @ViewChild ('uploader') uploader?: UploaderComponent;
  @ViewChild ('dropArea') dropArea?: HTMLElement;
  // Subscriptions
  private deployBuildSub: Subscription | undefined;

  @Input() selectedTreeNode = new TreeNode(-1, 'Dashboard', '', NodeType.None);
  @Output() showSpinner: EventEmitter<any> = new EventEmitter<any>();
  @Output() hideSpinner: EventEmitter<any> = new EventEmitter<any>();

  get disableDeploy(): boolean {
    return !(this.selectedTreeNode.id > 1 && this.selectedTreeNode.status == 'running'
      && this.uploader != undefined && this.uploader.getFilesData().length > 0 && this.uploader.getFilesData()[0].statusCode == '1');
  }

  constructor(private dashboardService: DashboardService) { }

  ngOnInit(): void { }

  ngOnDestroy(): void {
    this.deployBuildSub?.unsubscribe();
  }

  submit() {
    const msg = "Deploying a build containing any of the following will result in the removal of this server:<br/><br/>"
      + "- Malicious code<br/>"
      + "- Copyrighted material<br/>"
      + "- Pornographic material<br/><br/>";

    const dialog = DialogUtility.confirm({
      title: "Warning",
      content: msg,
      okButton: {
        text: "Continue",
        click: () => {
          this.deploy();
          dialog.close();
        }
      },
      showCloseIcon: true
    })
  }

  private deploy() {
    this.showSpinner.emit();
    let file: File = <File>this.uploader?.getFilesData(0)[0].rawFile;
    this.deployBuildSub = this.dashboardService.deployBuild(this.selectedTreeNode.id, file).subscribe(
      data => this.deploySuccessHandler(data),
      error => this.deployFailureHandler(error)
    )
  }

  private deploySuccessHandler(data: any) {
    this.hideSpinner.emit();

    DialogUtility.alert({
      title: 'Success',
      content: "Successfully deployed build.",
      showCloseIcon: true,
      closeOnEscape: true,
      animationSettings: { effect: 'Zoom' }
    });
  }

  private deployFailureHandler(error: any) {
    this.hideSpinner.emit();

    DialogUtility.alert({
      title: 'Error',
      content: "Failed to deploy build.",
      showCloseIcon: true,
      closeOnEscape: true,
      animationSettings: { effect: 'Zoom' }
    });
  }
}
