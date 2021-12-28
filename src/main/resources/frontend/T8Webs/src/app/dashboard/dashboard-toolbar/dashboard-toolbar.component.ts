import {AfterViewInit, Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {ToolbarComponent, ItemModel, ClickEventArgs} from "@syncfusion/ej2-angular-navigations";
import {ItemModel as ButtonItemModel} from '@syncfusion/ej2-angular-splitbuttons';
import {User} from "../dto/user";
import {NodeType, TreeNode} from "../dashboard-tree/TreeNode";
import {TemplateBinding} from "@angular/compiler";

@Component({
  selector: 'dashboard-toolbar',
  templateUrl: './dashboard-toolbar.component.html',
  styleUrls: ['./dashboard-toolbar.component.less']
})
export class DashboardToolbarComponent implements OnInit, AfterViewInit,OnChanges {
  removedMenuItems: ItemModel[] = [];
  // View elements
  @ViewChild('toolbar') toolbar?: ToolbarComponent;
  @ViewChild('loginAndOut') loginAndOut?: TemplateBinding;
  //@ViewChild('title') title?: HTMLElement;

  @Input() user: User | undefined;
  @Input() selectedTreeNode = new TreeNode(-1, 'Dashboard', NodeType.None);

  public editMenuItems: ButtonItemModel[] = [
    {
      text: 'Rename',
      iconCss: 'fa fa-pencil'
    },
    {
      text: 'Delete',
      iconCss: 'fa fa-trash'
    }
  ];

  public userMenuItems: ItemModel[] = [{text: 'Sign Out'}];

  public menuFields: Object = {
    text: ['header', 'text', 'value'],
    children: ['subItems', 'options']
  };

  constructor() { }

  ngOnInit(): void { }

  ngAfterViewInit(): void { }

  ngOnChanges(changes: SimpleChanges): void {
    const selectedTreeNode = changes['selectedTreeNode'];
    if(this.toolbar && selectedTreeNode && selectedTreeNode.currentValue) {
      const nodeType = selectedTreeNode.currentValue.type;
      const isGroup = nodeType === NodeType.ServerGroup || nodeType === NodeType.BalancerGroup;

      let index = 0;
      this.toolbar?.items.forEach((item: ItemModel) => {
        if(item.cssClass === 'group'){
          this.toolbar?.hideItem(index, !isGroup);
        } else if (item.cssClass === 'item') {
          this.toolbar?.hideItem(index, isGroup);
        }
        index++;
      });
    }
  }

  public created(): void {
    this.toolbar?.refreshOverflow();
  }

  public userMenuSelect(args: Event): void {
    this.logout();
  }

  public login(){
    window.location.replace("/oauth2/authorization/google");
  }

  public logout() {
    window.location.replace("/logout");
  }

  toolbarClick(args: ClickEventArgs) {
    switch (args.item.text) {
      case "Start": console.log("Start");
        break;
    }
  }

  editSelect(args: ClickEventArgs) {
    console.log(args.item.text);
  }
}
