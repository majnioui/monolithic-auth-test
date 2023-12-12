import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-test-github',
  templateUrl: './test-github.component.html',
  styleUrls: ['./test-github.component.scss'],
})
export class TestGithubComponent implements OnInit {
  responseMessage: string = '';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.getTestGithubResponse();
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
}
