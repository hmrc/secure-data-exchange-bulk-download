<table>
   <thead>
     <tr>
        <th>Information Type</th>
        <th>Description</th>
        <th>Content</th>
        <th>Frequency</th>
     </tr>
   </thead>
   <tbody>
     <tr>
        <td>TARIFF-ANNUAL</td>
        <td>Full extract of Taric3 and National Measures from database creation date to a certain point in time.</td>
        <td>Full Taric3 data (excluding certain EU measures) and National Measures</td>
        <td>Produced on 1st January every year at 00:30 and retained until 5th January the following year</td>
     </tr>
     <tr>
        <td>TARIFF-DAILY</td>
        <td>Delta of Taric3 and National Measures updates for that day. By the end of the month, there will be the number of files for the number of days in that month. The files will then be superseded by the monthly file in the next month.</td>
        <td>Daily update of Taric3 data (excluding certain EU measures) and daily National Measures</td>
        <td>Produced every day at 08:00 and retained until the 5th day of the following month</td>
     </tr>
     <tr>
       <td>TARIFF-MONTHLY</td>
       <td>Delta of Taric3 and National Measures updates for that month. A monthly file is a roll up of all the daily files for a month. By the end of each year, 12 monthly files will have been produced.</td>
       <td>Monthly update of Taric3 data (excluding certain EU measures) and monthly National Measures</td>
       <td>Produced on 1st day of the month at 21:00 and retained until the 5th January the following year</td>
     </tr>
   </tbody>
</table>