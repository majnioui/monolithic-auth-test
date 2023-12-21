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
  urlValue: string = '';

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

  saveAndSwitchPlatform(newPlatform: 'github' | 'gitlab') {
    this.AuthorizationService.saveClientUrl(this.urlValue, newPlatform).subscribe({
      next: () => {
        console.log('Client URL and platform type saved successfully');
      },
      error: error => {
        console.error('Error saving client URL and platform type', error);
      },
    });

    this.switchPlatform(newPlatform);
  }
  switchPlatform(newPlatform: 'github' | 'gitlab') {
    this.platform = newPlatform;
  }
}
