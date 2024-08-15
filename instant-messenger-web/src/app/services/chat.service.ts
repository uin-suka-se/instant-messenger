// src/app/services/chat.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ChatService {
  private apiUrl = 'http://localhost:8081';

  public chatDetailListener: BehaviorSubject<any> =
    new BehaviorSubject<boolean>(false);
  public chatDetailListener$ = this.chatDetailListener.asObservable();

  constructor(private http: HttpClient) {}

  chatDetailChanged() {
    this.chatDetailListener.next(true);
  }

  getChatList(nim: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/chatList`, { params: { nim: nim } });
  }

  getChatDetail(chatId: string, userId): Observable<any> {
    return this.http.get(`${this.apiUrl}/chatDetail`, {
      params: { chatId: chatId, userId: userId },
    });
  }

  getParticipants(chatId: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/participant`, { params: { chatId } });
  }

  sendMessage(message: any): Observable<any> {
    const options = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      }),
    };
    return this.http.post(
      `${this.apiUrl}/messages`,
      JSON.stringify(message),
      options
    );
  }

  downloadFile(fileId: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/download`, {
      responseType: 'blob',
      params: { fileId },
    });
  }
}
