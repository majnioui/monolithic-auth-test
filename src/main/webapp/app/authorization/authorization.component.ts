import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthorizationService } from '../services/authorization.service';

@Component({
  selector: 'app-test-github',
  templateUrl: './authorization.component.html',
  styleUrls: ['./authorization.component.scss'],
})
export class AuthorizationComponent implements OnInit {
  responseMessage: string = '';
  githubRepositories: any[] = [];
  gitlabRepositories: any[] = [];
  platform: 'github' | 'gitlab' = 'github';

  constructor(
    private http: HttpClient,
    private AuthorizationService: AuthorizationService,
  ) {}

  ngOnInit() {
    this.getGithubRepositories();
  }

  getGithubRepositories() {
    this.AuthorizationService.getGithubRepositories().subscribe({
      next: repos => {
        this.githubRepositories = repos;
      },
      error: error => {
        console.error('Component error fetching GitHub repositories:', error);
      },
    });

    this.AuthorizationService.getGitLabRepositories().subscribe({
      next: repos => {
        this.gitlabRepositories = repos;
      },
      error: error => {
        console.error('Component error fetching GitLab repositories:', error);
      },
    });
  }

  switchPlatform(newPlatform: 'github' | 'gitlab') {
    this.platform = newPlatform;
    this.getGithubRepositories();
  }
}
