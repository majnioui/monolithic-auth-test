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

  getBitbucketRepositories(): Observable<any[]> {
    return this.http.get<any[]>('/bitbucket/repositories');
  }

  saveClientUrl(clientUrl: string, platformType: string): Observable<any> {
    return this.http.post('/api/save-client-url', { clientUrl, platformType });
  }

  getJavaVersionFromGitHub(repoName: string, clientId: string): Observable<string> {
    return this.http.get<string>(`/api/github/java-version?repoName=${repoName}&clientId=${clientId}`);
  }
}
