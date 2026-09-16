import React, { createContext, useContext, useState } from 'react'
import api from '../api/axios'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('user')
    return stored ? JSON.parse(stored) : null
  })

  const login = async (email, password) => {
    const { data } = await api.post('/auth/login', { email, password })
    persist(data)
    return data
  }

  const register = async (payload) => {
    const { data } = await api.post('/auth/register', payload)
    persist(data)
    return data
  }

  const persist = (data) => {
    localStorage.setItem('token', data.token)
    const userObj = { email: data.email, fullName: data.fullName, role: data.role }
    localStorage.setItem('user', JSON.stringify(userObj))
    setUser(userObj)
  }

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
