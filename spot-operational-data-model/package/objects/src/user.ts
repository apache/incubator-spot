import {Email} from "./email";

export interface User {
    firstName: String;
    lastName: String;
    email: Email;
    address: String;
    location: String
    identifier: String;
    domain: String;
}
