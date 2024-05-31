import React, { useEffect } from 'react'
import { Route, Routes, useLocation, Navigate } from 'react-router-dom'
import AdminNavBar from './components/AdminNavBar'
import AdminLoginForm from './components/AdminLoginForm';
import CreateUser from './components/CreateUser';
import ShowUsers from './components/ShowUsers';
import CreateWallet from './components/CreateWallet';
import ShowWallets from './components/ShowWallets';

export default function App() {
  const token = localStorage.getItem('authToken');

  function RequireAuth({ children, redirectTo }) {
    let isAuthenticated = localStorage.getItem('authToken');
    return isAuthenticated ? children : <Navigate to={redirectTo} />;
  }
  return (
    <div>
      <Routes>

        <Route path='/admin' element={
          <RequireAuth redirectTo="/admin/login">
            <AdminNavBar />

          </RequireAuth>
        }>
          <Route path='/admin/create-user' element={<CreateUser />} />
          <Route path='/admin/show-users' element={<ShowUsers />} />
          <Route path='/admin/create-wallet' element={<CreateWallet />} />
          <Route path='/admin/show-wallets' element={<ShowWallets />} />

        </Route>
        <Route path='/admin/login' element={<AdminLoginForm />} />


      </Routes>
    </div>
  )
}
