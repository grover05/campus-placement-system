import React, { useEffect, useState } from 'react'
import api from '../../api/axios'
import Navbar from '../../components/Navbar'

export default function TpoDashboard() {
  const [drives, setDrives] = useState([])
  const [form, setForm] = useState({
    title: '', companyName: '', description: '', minPercentage: '', maxBacklogs: '',
    eligibleBranches: '', driveDate: '',
  })
  const [selectedDrive, setSelectedDrive] = useState(null)
  const [applications, setApplications] = useState([])
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  const loadDrives = async () => {
    try {
      const { data } = await api.get('/tpo/drives')
      setDrives(data)
    } catch (err) {
      setError('Failed to load drives')
    }
  }

  useEffect(() => { loadDrives() }, [])

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const createDrive = async (e) => {
    e.preventDefault()
    setError(''); setMessage('')
    try {
      await api.post('/tpo/drives', {
        title: form.title,
        companyName: form.companyName,
        description: form.description,
        minPercentage: parseFloat(form.minPercentage),
        maxBacklogs: parseInt(form.maxBacklogs, 10),
        eligibleBranches: form.eligibleBranches
          ? form.eligibleBranches.split(',').map((b) => b.trim()).filter(Boolean)
          : [],
        driveDate: form.driveDate,
      })
      setMessage('Drive posted.')
      setForm({ title: '', companyName: '', description: '', minPercentage: '', maxBacklogs: '', eligibleBranches: '', driveDate: '' })
      loadDrives()
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.details?.join(', ') || 'Failed to create drive')
    }
  }

  const viewApplications = async (drive) => {
    setSelectedDrive(drive)
    setError('')
    try {
      const { data } = await api.get(`/tpo/drives/${drive.id}/applications`)
      setApplications(data)
    } catch (err) {
      setError('Failed to load applications')
    }
  }

  const updateStatus = async (applicationId, status) => {
    try {
      await api.patch(`/tpo/applications/${applicationId}/status`, { status })
      viewApplications(selectedDrive)
    } catch (err) {
      setError('Failed to update status')
    }
  }

  const toggleDriveStatus = async (drive) => {
    const newStatus = drive.status === 'OPEN' ? 'CLOSED' : 'OPEN'
    try {
      await api.patch(`/tpo/drives/${drive.id}/status?status=${newStatus}`)
      loadDrives()
    } catch (err) {
      setError('Failed to update drive status')
    }
  }

  const exportShortlist = async (driveId) => {
    try {
      const response = await api.get(`/tpo/drives/${driveId}/export`, { responseType: 'blob' })
      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `shortlist-drive-${driveId}.csv`)
      document.body.appendChild(link)
      link.click()
      link.remove()
    } catch (err) {
      setError(err.response?.data?.message || 'No shortlisted students to export yet')
    }
  }

  return (
    <div>
      <Navbar />
      <div className="container">
        <h1>TPO Dashboard</h1>
        {message && <div className="card" style={{ background: '#dcfce7', color: '#166534' }}>{message}</div>}
        {error && <div className="error-banner">{error}</div>}

        <div className="card">
          <h3>Post a New Drive</h3>
          <form onSubmit={createDrive}>
            <div><label>Title</label><input name="title" required value={form.title} onChange={handleChange} /></div>
            <div><label>Company</label><input name="companyName" required value={form.companyName} onChange={handleChange} /></div>
            <div><label>Description</label><textarea name="description" rows={3} value={form.description} onChange={handleChange} /></div>
            <div><label>Min Percentage</label><input name="minPercentage" type="number" step="0.01" required value={form.minPercentage} onChange={handleChange} /></div>
            <div><label>Max Backlogs Allowed</label><input name="maxBacklogs" type="number" min="0" required value={form.maxBacklogs} onChange={handleChange} /></div>
            <div><label>Eligible Branches (comma-separated, blank = all)</label><input name="eligibleBranches" value={form.eligibleBranches} onChange={handleChange} placeholder="Computer Science, Information Technology" /></div>
            <div><label>Drive Date</label><input name="driveDate" type="date" required value={form.driveDate} onChange={handleChange} /></div>
            <button className="primary" type="submit">Post Drive</button>
          </form>
        </div>

        <div className="card">
          <h3>All Drives</h3>
          <table>
            <thead><tr><th>Company</th><th>Role</th><th>Status</th><th>Date</th><th></th></tr></thead>
            <tbody>
              {drives.map((d) => (
                <tr key={d.id}>
                  <td>{d.companyName}</td>
                  <td>{d.title}</td>
                  <td><span className={`badge ${d.status.toLowerCase()}`}>{d.status}</span></td>
                  <td>{d.driveDate}</td>
                  <td>
                    <button className="secondary" onClick={() => viewApplications(d)}>View Applicants</button>{' '}
                    <button className="secondary" onClick={() => toggleDriveStatus(d)}>{d.status === 'OPEN' ? 'Close' : 'Reopen'}</button>{' '}
                    <button className="secondary" onClick={() => exportShortlist(d.id)}>Export Shortlist</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {selectedDrive && (
          <div className="card">
            <h3>Applicants for {selectedDrive.title} @ {selectedDrive.companyName}</h3>
            {applications.length === 0 ? (
              <p className="muted">No applications yet.</p>
            ) : (
              <table>
                <thead><tr><th>Name</th><th>Branch</th><th>%</th><th>Backlogs</th><th>Status</th><th></th></tr></thead>
                <tbody>
                  {applications.map((a) => (
                    <tr key={a.id}>
                      <td>{a.studentName}</td>
                      <td>{a.branch}</td>
                      <td>{a.percentage}</td>
                      <td>{a.backlogs}</td>
                      <td><span className={`badge ${a.status.toLowerCase()}`}>{a.status}</span></td>
                      <td>
                        <button className="secondary" onClick={() => updateStatus(a.id, 'SHORTLISTED')}>Shortlist</button>{' '}
                        <button className="danger" onClick={() => updateStatus(a.id, 'REJECTED')}>Reject</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
