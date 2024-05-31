import React, { useState } from 'react';

import axios from 'axios';

export default function CreateUser() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');


  const handleSubmit = async (e) => {
    e.preventDefault();
    const response = await axios.post('http://localhost:8080/api/admin/register', {
        username,
        password
    })
        .then((response) => {
            setError("");
            console.log(response);
        })
        .catch((error) => {
            setError(error);
        })
}

  return (
    <div className='flex flex-col'>
      <div className='flex p-2 mb-8 font-bold text-2xl w-full'>
        Создать пользователя
      </div>
      <form onSubmit={handleSubmit} className='mx-4'>
        <div className="mb-4">
          <label className=" text-gray-700 font-bold mb-2" htmlFor="username">
            Имя пользователя
          </label>
          <input
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
            id="username"
            type="text"
            placeholder="Введите имя пользователя"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
        </div>
        <div className="mb-4">
          <label className="text-gray-700 font-bold mb-2" htmlFor="password">
            Пароль
          </label>
          <input
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
            id="password"
            type="password"
            placeholder="Введите имя пользователя"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>
        <div className="flex items-center justify-between">
          <button
            className="w-full bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
            type="submit"
          >
            Создать
          </button>
        </div>

      </form>
      {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            Asdasdasdas
          </div>
        )}
    </div>
  )
}
