//
// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

var DateUtils = {
  /**
   *  @param {Date} date      A base point in time for calculation
   *  @param {Number} delta   An integer, positive or negative, to calculate the new date
   *  @param {String} unit    One of 'day', 'month', 'year'
   *
   *  @returns A new date, delta days before/after date
   */
  calcDate: function (date, delta, unit)
  {
      var year, month, day, newDate, now;

      unit  = unit || 'day';

      year = date.getFullYear();
      month = date.getMonth();
      day = date.getDate();

      switch (unit)
      {
          case 'year':
              year += delta;
              break;
          case 'month':
              month += delta;
              break;
          case 'day':
              day += delta;
              break;
      }

      now = new Date();
      newDate = new Date(year, month, day, date.getHours(), date.getMinutes(), date.getSeconds(), date.getMilliseconds());

      // Make sure we dont return a date in the future
      return newDate < now ? newDate : now;
  },
  /**
   *  Returns a UI friendly representation of a data
   *
   *  @param {Date} date  A date object to format
   *
   *  @return {String} A string representing a Date object
   **/
  formatDate: function (date)
  {
      return date.toISOString().substr(0, 10);
  },
  /**
   *  Creates a Date object from a string
   *
   *  @param {String} dateStr     A string containing date information
   *
   *  @return {Date}  A date object represetation of the string
   **/
  parseDate: function (dateStr)
  {
      return new Date(Date.parse(dateStr + new Date().toString().substr(33)));
  }
};

module.exports = DateUtils;
