import { Injectable } from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpParams} from "@angular/common/http";
import {Observable, throwError} from "rxjs";
import {catchError} from "rxjs/operators";
import {User} from "./dto/user";

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

  getServers(): Observable<any> {
    const url = '/servers';
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
}
