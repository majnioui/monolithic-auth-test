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
  repositories: any[] = []; // Array to store GitHub repositories

  constructor(
    private http: HttpClient,
    private githubService: GithubService, // Inject the GithubService
  ) {}

  ngOnInit() {
    this.getTestGithubResponse();
    this.getUserRepositories();
  }

  getTestGithubResponse() {
    console.log('Making request to /testgit');
    this.http.get('/testgit', { responseType: 'text' }).subscribe({
      next: response => {
        this.responseMessage = response;
      },
      error: error => {
        console.error('There was an error!', error);
      },
    });
  }

  getUserRepositories() {
    this.githubService.getUserRepositories().subscribe({
      next: repos => {
        this.repositories = repos;
      },
      error: error => {
        console.error('Compenant error fetching repositories:', error);
      },
    });
  }
}
