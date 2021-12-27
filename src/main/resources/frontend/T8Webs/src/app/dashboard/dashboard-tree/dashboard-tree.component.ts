import {Component, EventEmitter, OnInit, Output, ViewChild} from '@angular/core';
import {ContextMenuComponent, NodeClickEventArgs, TreeViewComponent, MenuEventArgs, MenuItemModel, BeforeOpenCloseMenuEventArgs} from "@syncfusion/ej2-angular-navigations";


@Component({
  selector: 'dashboard-tree',
  templateUrl: './dashboard-tree.component.html',
  styleUrls: ['./dashboard-tree.component.less']
})
export class DashboardTreeComponent implements OnInit {
  // View Elements
  @ViewChild ('treeView') treeView!: TreeViewComponent;
  @ViewChild ('contextMenu') contextMenu!: ContextMenuComponent;

  @Output() openAddServer: EventEmitter<any> = new EventEmitter<any>();

  public treeData: Object[] = [
    { id: 'servers', name: 'Servers', expanded: true,
      subChild: [
        {id: '101', name: 'GurrCannabis'},
        {id: '102', name: 'DiabloWiki'}
      ]
    },
    {
      id: 'loadBalancers', name: 'Load Balancers',
      subChild: [
        {id: '201', name: 'Alex'}
      ]
    }
  ];

  public treeFields: Object ={ dataSource: this.treeData, id: 'id', text: 'name', child: 'subChild', htmlAttributes: 'hasAttribute' };

  public contextMenuItems: MenuItemModel[] = [{ text: 'Add' }];

  constructor() { }

  ngOnInit(): void { }

  public nodeClicked(args: NodeClickEventArgs) {
    const nodeId = args.node.getAttribute('data-uid');
    if (args.event.which === 3 && nodeId) {
      this.treeView.selectedNodes = [nodeId];
    }
  }

  public menuClick(args: MenuEventArgs) {
    let targetNodeId: string = this.treeView?.selectedNodes[0];
    if (args.item.text == "Add") {
      this.openAddServer.emit();
    }
  }

  public beforeOpen(args: BeforeOpenCloseMenuEventArgs) {
    let targetNodeId: string = this.treeView.selectedNodes[0];
    let targetNode: Element | null = document.querySelector('[data-uid="' + targetNodeId + '"]');

    if (targetNodeId !== 'servers') {
      this.contextMenu.hideItems(['Add']);
    } else {
      this.contextMenu.showItems(['Add']);
    }
  }

}
