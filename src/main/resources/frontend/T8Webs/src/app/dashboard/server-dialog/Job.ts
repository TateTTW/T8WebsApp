
export enum JobType {
  LoadBalancer = "Load Balancer",
  Server = "Server",
  None = ""
}

export enum JobAction {
  Add = "Add",
  Rename = "Rename",
  None= ""
}

export interface Job {
  type: JobType;
  action: JobAction;
  vmid: number;
}
