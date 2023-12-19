import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthorizationService {
  constructor(private http: HttpClient) {}

  getGithubRepositories(): Observable<any[]> {
    return this.http.get<any[]>('/github/repositories');
  }

  getGitLabRepositories(): Observable<any[]> {
    return this.http.get<any[]>('/gitlab/repositories');
  }
}
