import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class StatsService {
  private resourceUrl = '/api/stats';

  constructor(private http: HttpClient) {}

  getWebsiteMonitoringConfig(): Observable<any> {
    return this.http.get(this.resourceUrl);
  }

  getHostAgentDetails(): Observable<any> {
    return this.http.get('/api/host-agent');
  }

  getInstalledSoftware(): Observable<any> {
    return this.http.get('/api/installed-software');
  }

  getInfrastructureTopology(): Observable<any> {
    return this.http.get('/api/infra-topology');
  }

  getAllEvents(): Observable<any> {
    return this.http.get('/api/all-events');
  }
}
