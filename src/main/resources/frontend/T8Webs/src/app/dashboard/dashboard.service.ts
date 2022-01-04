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
    return this.http.get(url, options).pipe(catchError(this.handleError))
  }

  private putData(url: string, options: any): Observable<any> {
    return this.http.put(url, undefined, options).pipe(catchError(this.handleError))
  }

  private putBodyData(url: string, body: any, options: any): Observable<any> {
    return this.http.put(url, body, options).pipe(catchError(this.handleError))
  }

  private postBodyData(url: string, body: any, options: any): Observable<any> {
    return this.http.post(url, body, options).pipe(catchError(this.handleError))
  }

  private handleError(err: HttpErrorResponse) {
    let errorMessage = "";

    if(err.error instanceof ErrorEvent){
      errorMessage = `An error occurred: ${err.error.message}`;
    } else {
      errorMessage = `Server returned code: ${err.status}, Error message is: ${err.message}`;
    }

    return throwError(errorMessage)
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
    return this.getData(url, options);
  }

  getUser(): Observable<User> {
    const url = '/user';
    return this.getData(url, undefined);
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
