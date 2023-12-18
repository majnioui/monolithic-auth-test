// src/app/test-github/test-github.component.ts

import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { GithubService } from '../services/github.service';

@Component({
  selector: 'app-test-github',
  templateUrl: './test-github.component.html',
  styleUrls: ['./test-github.component.scss'],
})
export class TestGithubComponent implements OnInit {
  responseMessage: string = '';
  repositories: any[] = [];
  platform: 'github' | 'gitlab' = 'github';

  constructor(
    private http: HttpClient,
    private githubService: GithubService,
  ) {}

  ngOnInit() {
    this.getTestGithubResponse();
    this.getUserRepositories();
  }

  getTestGithubResponse() {
    const endpoint = this.platform === 'github' ? '/testgit' : '/testgitlab';
    console.log(`Making request to ${endpoint}`);
    this.http.get(endpoint, { responseType: 'text' }).subscribe({
      next: response => {
        this.responseMessage = response;
      },
      error: error => {
        console.error('There was an error!', error);
      },
    });
  }

  getUserRepositories() {
    if (this.platform === 'github') {
      this.githubService.getUserRepositories().subscribe({
        next: repos => {
          this.repositories = repos;
        },
        error: error => {
          console.error('Component error fetching GitHub repositories:', error);
        },
      });
    } else if (this.platform === 'gitlab') {
      this.githubService.getGitLabRepositories().subscribe({
        next: repos => {
          this.repositories = repos;
        },
        error: error => {
          console.error('Component error fetching GitLab repositories:', error);
        },
      });
    }
  }

  switchPlatform(newPlatform: 'github' | 'gitlab') {
    this.platform = newPlatform;
    this.getTestGithubResponse();
    this.getUserRepositories();
  }
}
