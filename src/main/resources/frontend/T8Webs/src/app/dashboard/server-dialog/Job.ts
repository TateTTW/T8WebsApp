
export enum JobType {
  LoadBalancer = "Load Balancer",
  Server = "Server",
  None = ""
}

export enum JobAction {
  Add = "Add",
  Start = "Start",
  Stop = "Stop",
  Reboot = "Reboot",
  Rename = "Rename",
  Delete = "Delete",
  None= ""
}

export class Job {
  type = JobType.None;
  action = JobAction.None;
  vmid = -1;

  public static findJobAction(jobAction: string | undefined): JobAction {
    switch (jobAction) {
      case JobAction.Add: return JobAction.Add;
      case JobAction.Start: return JobAction.Start;
      case JobAction.Stop: return JobAction.Stop;
      case JobAction.Reboot: return JobAction.Reboot;
      case JobAction.Rename: return JobAction.Rename;
      case JobAction.Delete: return JobAction.Delete;
      default: return JobAction.None;
    }
  }
}
