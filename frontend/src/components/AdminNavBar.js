import React from 'react'
import { NavLink, Outlet } from 'react-router-dom'

import { FaUserPlus, FaUserFriends, FaWallet, FaMoneyBillAlt } from 'react-icons/fa';
import { GrLogout } from "react-icons/gr";

const logout = () => {
    window.location.href = "/admin/login";
    window.localStorage.clear();
}
export default function AdminNavBar() {

    return (
        <div className='flex h-screen'>

            <nav className="bg-gray-800 text-white p-4 flex flex-col justify-between">
                <div className="">
                    <h2 className="text-xl font-bold mb-4 bg-slate-100 text-black rounded-lg p-2">Панель оператора</h2>
                    <ul >
                        <li className="mb-2">
                            <NavLink to="create-user" className="pr-16 flex items-center hover:bg-gray-700 rounded-md p-2">
                                <FaUserPlus className="mr-2" />
                                Создать пользователя
                            </NavLink>
                        </li>
                        <li className="mb-2">
                            <NavLink to="show-users" className="pr-16 flex items-center hover:bg-gray-700 rounded-md p-2">
                                <FaUserFriends className="mr-2" />
                                Список пользователей
                            </NavLink>
                        </li>
                        <li className="mb-2">
                            <NavLink to="create-wallet" className="pr-16 flex items-center hover:bg-gray-700 rounded-md p-2">
                                <FaWallet className="mr-2" />
                                Создать кошелёк
                            </NavLink>
                        </li>
                        <li className="mb-2">
                            <NavLink to="show-wallets" className="pr-16 flex items-center hover:bg-gray-700 rounded-md p-2">
                                <FaMoneyBillAlt className="mr-2" />
                                Список кошельков
                            </NavLink>
                        </li>

                    </ul>
                </div>
                <button onClick={logout} className="pr-16 flex items-center hover:bg-gray-700 rounded-md p-2">
                    <GrLogout className="mr-2" />
                    Выйти
                </button>
            </nav>

            <div>
                <Outlet />
            </div>

        </div>
    )
}
