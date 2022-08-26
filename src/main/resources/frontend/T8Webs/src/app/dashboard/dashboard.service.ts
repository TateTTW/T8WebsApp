import { Injectable } from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpParams} from "@angular/common/http";
import {Observable, throwError} from "rxjs";
import {catchError} from "rxjs/operators";
import {User} from "./dto/user";
import {NetData} from "./server-content/content/net-graph/net-graph.component";

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  constructor(private http: HttpClient) { }

  private getData(url: string, options: any): Observable<any> {
    return this.http.get(url, options);
  }

  private putData(url: string, options: any): Observable<any> {
    return this.putBodyData(url, undefined, options);
  }

  private postData(url: string, options: any): Observable<any> {
    return this.postBodyData(url, undefined, options);
  }

  private patchData(url: string, options: any): Observable<any> {
    return this.patchBodyData(url, undefined, options);
  }

  private deleteData(url: string, options: any): Observable<any> {
    return this.http.delete(url, options);
  }

  private putBodyData(url: string, body: any, options: any): Observable<any> {
    return this.http.put(url, body, options);
  }

  private postBodyData(url: string, body: any, options: any): Observable<any> {
    return this.http.post(url, body, options);
  }

  private patchBodyData(url: string, body: any, options: any): Observable<any> {
    return this.http.patch(url, body, options);
  }

  createHttpParams(params: any): HttpParams {
    let httpParams: HttpParams = new HttpParams();
    Object.keys(params).forEach(param => {
      if(params[param]){
        httpParams = httpParams.set(param, params[param]);
      }
    });

    return httpParams;
  }

  grantAccess(userId: string): Observable<any> {
    const url = '/grantAccess';
    const params = {userId: userId};
    const options = { params: this.createHttpParams(params)};
    return this.patchData(url, options);
  }

  revokeAccess(userId: string): Observable<any> {
    const url = '/revokeAccess';
    const params = {userId: userId};
    const options = { params: this.createHttpParams(params)};
    return this.patchData(url, options);
  }

  requestAccess(): Observable<any> {
    const url = '/requestAccess';
    return this.patchData(url, undefined);
  }

  addServer(serverName: string): Observable<any> {
    const url = '/addServer';
    const params = {serverName: serverName};
    const options = { params: this.createHttpParams(params)};
    return this.postData(url, options);
  }

  deleteServer(vmid: number): Observable<any> {
    const url = '/deleteServer';
    const params = {vmid: vmid};
    const options = { params: this.createHttpParams(params)};
    return this.deleteData(url, options);
  }

  forceDeleteServer(vmid: number): Observable<any> {
    const url = '/forceDeleteServer';
    const params = {vmid: vmid};
    const options = { params: this.createHttpParams(params)};
    return this.deleteData(url, options);
  }

  renameServer(vmid: number, name: string): Observable<any> {
    const url = '/renameServer';
    const params = {
      vmid: vmid,
      name: name
    };
    const options = { params: this.createHttpParams(params)};
    return this.putData(url, options);
  }

  startServer(vmid: number): Observable<any> {
    const url = '/startServer';
    const params = {vmid: vmid};
    const options = { params: this.createHttpParams(params)};
    return this.patchData(url, options);
  }

  stopServer(vmid: number): Observable<any> {
    const url = '/stopServer';
    const params = {vmid: vmid};
    const options = { params: this.createHttpParams(params)};
    return this.patchData(url, options);
  }

  rebootServer(vmid: number): Observable<any> {
    const url = '/rebootServer';
    const params = {vmid: vmid};
    const options = { params: this.createHttpParams(params)};
    return this.patchData(url, options);
  }

  getUser(): Observable<User> {
    const url = '/user';
    return this.getData(url, undefined);
  }

  getAllUsers(): Observable<any> {
    const url = '/allUsers';
    return this.getData(url, undefined);
  }

  getTree(): Observable<any> {
    const url = '/tree';
    return this.getData(url, undefined);
  }

  getAllServers(): Observable<any> {
    const url = '/allServers';
    return this.getData(url, undefined);
  }

  getServerStatus(vmid: number): Observable<any> {
    const url = '/serverStatus';
    const params = {vmid: vmid};
    const options = { params: this.createHttpParams(params)};
    return this.getData(url, options);
  }

  deployBuild(vmid: number, file: File): Observable<any> {
    const url = '/deployBuild';

    let headers = new Headers();
    /** In Angular 5, including the header Content-Type can invalidate your request */
    headers.append('Content-Type', 'multipart/form-data');
    headers.append('Accept', 'application/json');

    const params = {vmid: vmid};

    let formData:FormData = new FormData();
    formData.append('buildFile', file, "build.war");

    let options = { headers: headers, params: this.createHttpParams(params) };

    return this.postBodyData(url, formData, options)
  }

  getServerData(vmid: number): Observable<any> {
    const url = '/serverData';
    const params = {vmid: vmid};
    const options = { params: this.createHttpParams(params)};
    return this.getData(url, options);
  }

  getUsersServerData(vmid: number, userId: string): Observable<any> {
    const url = '/usersServerData';
    const params = {vmid: vmid, userId: userId};
    const options = { params: this.createHttpParams(params)};
    return this.getData(url, options);
  }

  mapServerData(response: any): {netData: NetData, cpuData: { x: Date, y: string }[], ramData: { x: Date, y: string }[]} {
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

    return {netData: netData, cpuData: cpuData, ramData: ramData};
  }
}
