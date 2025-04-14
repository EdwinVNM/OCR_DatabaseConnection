This Android Application extracts Invoice Details from Invoices and sends them to a MySQL Database (XAMPP) where you can later edit/filter these invoices.


How to run it:

1. Download Zip File of this github repository and extract it.
2. Open the application on your android phone/emulator through Android Studio.
3. Run Apache and MySQL through XAMPP ports 80, 443 and 3307.
4. Run 'invoice' and 'user_login' SQL Scripts through MySQL Workbench.
5. Run OCR_Database Connection as a server through Eclipse (Need Apache Tomcat as Server).
6. If you need to change the IP Address in IPAddressSingleton you can use POSTMAN for debuging. (The IP Address for the emulator: 10.0.2.2)
   http://<YourLaptopsIPv4//cmd/ipconfig>:8080/OCR_DatabaseConnection/invoice?id=5 should return something like: [
    {
        "id": 5,
        "customer_name": "John B",
        "invoice_date": "26/03/25",
        "due_date": "28/03/25",
        "total_amount": 11.00,
        "status": "Partially"
    }
]

![image](https://github.com/user-attachments/assets/8895e355-6ec6-4d25-8ebf-4cde27d1fcff)
