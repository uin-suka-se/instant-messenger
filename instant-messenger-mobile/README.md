## How the Mobile Development works in this application
   - In Android app development, the use of ViewModel and Repository is part of the recommended architecture, commonly known as the MVVM (Model-View-ViewModel) pattern. 
     This pattern helps in separating concerns, making the codebase more modular, testable, and maintainable.
   - The ApiService interface is responsible for defining the endpoints and operations related to API.
   - The ViewModel is responsible for managing UI-related data. It holds and processes data needed by the UI and survives configuration changes (like screen rotation). 
     It interacts with the Repository to fetch or save data.
   - The activities called the functions from the view models, so the data will survive from any configuration changes (like device rotation) without the need to reload the data.

   - In conclusion, ApiService defines the API endpoints. Repository uses ApiService to fetch data and provides additional data handling logic if needed. ViewModel interacts with the Repository to fetch data. It
     exposes the fetched data to the UI through a LiveData or StateFlow.
