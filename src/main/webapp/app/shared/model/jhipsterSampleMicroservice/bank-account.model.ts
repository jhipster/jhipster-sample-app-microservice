

export interface IBankAccount {
    id?: number;
    name?: string;
    balance?: number;
}

export class BankAccount implements IBankAccount {
    constructor(
        public id?: number,
        public name?: string,
        public balance?: number,
    ) {
    }
}
