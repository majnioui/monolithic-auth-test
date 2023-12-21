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

  saveClientUrl(clientUrl: string, platformType: string): Observable<any> {
    return this.http.post('/api/save-client-url', { clientUrl, platformType });
  }

  generatePullRequest(platform: 'github' | 'gitlab', repoName: string): Observable<any> {
    const url = `/api/generate-pr`;
    const payload = { platform, repoName };
    return this.http.post(url, payload);
  }
}
